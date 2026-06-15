const axios = require('axios'); // <-- Neu: Axios für den sicheren Download
const pLimit = require('p-limit'); // Concurrency pool
const csv = require('csv-parser');
const { initializeApp, cert, applicationDefault } = require('firebase-admin/app');
const { getFirestore, GeoPoint } = require('firebase-admin/firestore');
const { Client } = require('@googlemaps/google-maps-services-js');
const fs = require('fs');
const path = require('path');

// 1. Firebase initialisieren
let firebaseConfig = {};
const keyPath = path.join(__dirname, 'firebase-key.json');

if (fs.existsSync(keyPath)) {
  const serviceAccount = require(keyPath);
  firebaseConfig.credential = cert(serviceAccount);
} else {
  console.log('Keine firebase-key.json gefunden. Verwende Application Default Credentials...');
  firebaseConfig.credential = applicationDefault();
  firebaseConfig.projectId = process.env.GOOGLE_CLOUD_PROJECT || process.env.GCLOUD_PROJECT || 'abgabestellen-berlin';
}

initializeApp(firebaseConfig);
const db = getFirestore();

// 2. Google Maps Client initialisieren
const mapsClient = new Client({});
// Der Key kommt bei GitHub Actions nun aus den Secrets!
const GOOGLE_MAPS_API_KEY = process.env.MAPS_API_KEY; 

const SHEET_CSV_URL = 'https://docs.google.com/spreadsheets/d/e/2PACX-1vQDm_02PGznjHSfVb31ZLArm5Mb16UiIP5KkeMFojkAfU_9ggP5kxyyJfxGZF2DRNjZJwNUgxv7y2oT/pub?gid=0&single=true&output=csv';

async function importData() {
  console.log('Lade Daten direkt aus Google Sheets herunter...');
  
  try {
    const results = [];
    
    // Axios lädt die Datei herunter und folgt den Google-Umleitungen automatisch
    const response = await axios({
        method: 'get',
        url: SHEET_CSV_URL,
        responseType: 'stream'
    });

    // Wrap the stream in a Promise to ensure proper async handling
    await new Promise((resolve, reject) => {
      response.data.pipe(csv())
        .on('data', (data) => results.push(data))
        .on('end', async () => {
          try {
            console.log(`${results.length} Zeilen aus Google Sheets geladen.`);
            
            if (results.length === 0) {
                console.log('Abbruch: Es wurden keine Daten gefunden. Bitte den Link prüfen!');
                resolve();
                return;
            }

            console.log('Lösche veraltete Einträge aus Firebase...');
            const snapshot = await db.collection('abgabestellen').get();

            // Optimierte Löschung mit Batches (max 500 Operationen pro Batch)
            const batches = [];
            let currentBatch = db.batch();
            let operationCount = 0;

            for (const doc of snapshot.docs) {
                currentBatch.delete(doc.ref);
                operationCount++;

                if (operationCount === 500) {
                    batches.push(currentBatch.commit());
                    currentBatch = db.batch();
                    operationCount = 0;
                }
            }

            if (operationCount > 0) {
                batches.push(currentBatch.commit());
            }

            await Promise.all(batches);
            console.log('Datenbank geleert. Starte neuen Import...');

            const limit = pLimit(10); // Concurrency pool of 10 for Geocoding API limits
            let importBatch = db.batch();
            let importOperationCount = 0;
            const importBatches = [];

            const promises = results.map((row) => limit(async () => {
                const anschrift = row['Anschrift'];
                const plz = row['PLZ'];
                const name = row['Abgabestelle (schwarz:aktualisiert, rot:in Aktualisierung, lila: momentan außer Betrieb)'];

                if (!anschrift || !name) return null;

                const fullAddress = `${anschrift}, ${plz} Berlin, Germany`;
                let lat = null, lng = null;

                try {
                  const mapResponse = await mapsClient.geocode({
                    params: {
                      address: fullAddress,
                      key: GOOGLE_MAPS_API_KEY,
                    }
                  });

                  if (mapResponse.data.results.length > 0) {
                    lat = mapResponse.data.results[0].geometry.location.lat;
                    lng = mapResponse.data.results[0].geometry.location.lng;
                  }
                } catch (error) {
                  console.error(`Fehler beim Geocoding für ${fullAddress}:`, error.message);
                }

                const docData = {
                   name: name,
                   anschrift: anschrift,
                   plz: plz,
                   ortsteil: row['Ortsteil'] || '',
                   ansprechpartner: row['AnsprechpartnerIn'] || '',
                   telefon: row['Telefon'] || row['Telefon / Mail'] || '',
                   annahmezeiten: row['Wann können Lebensmittel abgegeben werden?'] || '',
                   akzeptiert: row['Was wird angenommen?'] || '',
                   anmeldung_noetig: (row['vorherige telefon. Anmeldung nötig?'] === 'TRUE' || row['Anmeldung erforderlich?'] === 'TRUE'),
                   bemerkungen: row['Bemerkungen'] || '',
                };

                if (lat && lng) {
                    docData.location = new GeoPoint(lat, lng);
                }

                return { name, docData };
            }));

            const resolvedDocs = await Promise.all(promises);

            for (const item of resolvedDocs) {
              if (!item) continue;

              const docRef = db.collection('abgabestellen').doc();
              importBatch.set(docRef, item.docData);
              importOperationCount++;
              console.log(`Verarbeitet: ${item.name}`);

              if (importOperationCount === 500) {
                  importBatches.push(importBatch.commit());
                  importBatch = db.batch();
                  importOperationCount = 0;
              }
            }

            if (importOperationCount > 0) {
                importBatches.push(importBatch.commit());
            }

            await Promise.all(importBatches);
            console.log('Synchronisation erfolgreich abgeschlossen!');
            resolve();
          } catch (error) {
            reject(error);
          }
        })
        .on('error', (error) => {
          reject(error);
        });
    });
  } catch (error) {
      console.error("Fehler beim Herunterladen der CSV:", error.message);
      process.exit(1);
  }
}

importData();
