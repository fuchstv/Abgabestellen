const { initializeApp } = require('firebase-admin/app');
const { getFirestore, GeoPoint } = require('firebase-admin/firestore');
const { Client } = require('@googlemaps/google-maps-services-js');
const axios = require('axios');
const pLimit = require('p-limit');
const csv = require('csv-parser');
const functions = require('@google-cloud/functions-framework');

// 1. Initialize Firebase Admin SDK
// This automatically picks up the service account credentials when running in Google Cloud
initializeApp();
const db = getFirestore();

// 2. Google Maps Client
const mapsClient = new Client({});
const GOOGLE_MAPS_API_KEY = process.env.MAPS_API_KEY;

const SHEET_CSV_URL = 'https://docs.google.com/spreadsheets/d/e/2PACX-1vQDm_02PGznjHSfVb31ZLArm5Mb16UiIP5KkeMFojkAfU_9ggP5kxyyJfxGZF2DRNjZJwNUgxv7y2oT/pub?gid=0&single=true&output=csv';

async function importData() {
  console.log('Lade Daten direkt aus Google Sheets herunter...');
  
  const results = [];
  
  const response = await axios({
      method: 'get',
      url: SHEET_CSV_URL,
      responseType: 'stream'
  });

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

          // Batch delete (max 500 operations per batch)
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

          const limit = pLimit(10); // Limit concurrency to respect Google Maps API rate limits
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
}

// 3. Register the HTTP trigger
functions.http('importDataHttp', async (req, res) => {
  // Simple token authorization check
  const authHeader = req.headers.authorization;
  const secretToken = process.env.SCHEDULER_SECRET_TOKEN;
  
  if (secretToken && authHeader !== `Bearer ${secretToken}`) {
    console.warn('Unauthorized trigger attempt.');
    return res.status(401).send('Unauthorized');
  }

  try {
    await importData();
    res.status(200).send('Synchronization successful');
  } catch (error) {
    console.error('Error during import:', error);
    res.status(500).send(`Error during import: ${error.message}`);
  }
});
