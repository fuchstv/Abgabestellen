const https = require('https');
const csv = require('csv-parser');
const admin = require('firebase-admin');
const { Client } = require('@googlemaps/google-maps-services-js');

// 1. Firebase initialisieren
const serviceAccount = require('./firebase-key.json'); // Dein heruntergeladener Schlüssel
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});
const db = admin.firestore();

// 2. Google Maps Client initialisieren
const mapsClient = new Client({});
// WICHTIG: Trage hier wieder deinen API-Key (in Anführungszeichen) ein!
const GOOGLE_MAPS_API_KEY = process.env.MAPS_API_KEY; 

// Der direkte Link zu deiner Live-CSV aus Google Sheets
const SHEET_CSV_URL = 'https://docs.google.com/spreadsheets/d/e/2PACX-1vSfdgEDSS9A-uChmLWRAgqdSAzC31eTl9bFGuKYcXQtyYOwVIArCI2ph4oUoMTK7sOHNoRNkajY4f0q/pub?output=csv';

async function importData() {
  console.log('Lade Daten direkt aus Google Sheets herunter...');
  const results = [];
  
  // Lädt die Live-CSV direkt aus dem Google Sheet
  https.get(SHEET_CSV_URL, (response) => {
    response.pipe(csv())
      .on('data', (data) => results.push(data))
      .on('end', async () => {
        console.log(`${results.length} Zeilen aus Google Sheets geladen.`);
        
        // --- NEU: Alte Einträge in Firebase löschen, um Duplikate zu vermeiden ---
        console.log('Lösche veraltete Einträge aus Firebase...');
        const snapshot = await db.collection('abgabestellen').get();
        // Wir löschen alle alten Dokumente parallel
        await Promise.all(snapshot.docs.map(doc => doc.ref.delete()));
        console.log('Datenbank geleert. Starte neuen Import...');

        // --- Dein bisheriger Import- und Geocoding-Code ---
        for (const row of results) {
          const anschrift = row['Anschrift'];
          const plz = row['PLZ'];
          const name = row['Abgabestelle (schwarz:aktualisiert, rot:in Aktualisierung, lila: momentan außer Betrieb)'];

          if (!anschrift || !name) continue; // Leere Zeilen überspringen

          const fullAddress = `${anschrift}, ${plz} Berlin, Germany`;
          let lat = null, lng = null;

          try {
            // Geocoding: Adresse -> Koordinaten
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

          // Sauberes Datenobjekt für Firestore zusammenstellen
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
              docData.location = new admin.firestore.GeoPoint(lat, lng);
          }

          await db.collection('abgabestellen').add(docData);
          console.log(`Importiert: ${name}`);
          
          await new Promise(resolve => setTimeout(resolve, 200)); 
        }
        
        console.log('Synchronisation erfolgreich abgeschlossen!');
      });
  });
}

// Führt die Funktion am Ende aus
importData();