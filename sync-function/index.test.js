const crypto = require("crypto");

let registeredHandler = null;

jest.mock('@google-cloud/functions-framework', () => {
    return {
        http: (name, handler) => {
            if (name === 'importDataHttp') {
                registeredHandler = handler;
            }
        }
    };
});

jest.mock('firebase-admin/app', () => ({
    initializeApp: jest.fn(),
}));

const mockBatch = {
    delete: jest.fn(),
    set: jest.fn(),
    commit: jest.fn().mockResolvedValue(),
};

const mockDb = {
    collection: jest.fn(() => ({
        get: jest.fn().mockResolvedValue({ docs: [] }),
        doc: jest.fn(() => 'mock-doc-ref'),
    })),
    batch: jest.fn(() => mockBatch),
};

jest.mock('firebase-admin/firestore', () => ({
    getFirestore: jest.fn(() => mockDb),
    GeoPoint: jest.fn(),
}));

const mockMapsClient = {
    geocode: jest.fn().mockResolvedValue({ data: { results: [] } })
};

jest.mock('@googlemaps/google-maps-services-js', () => ({
    Client: jest.fn(() => mockMapsClient)
}));

jest.mock('axios');
const axios = require('axios');
const { Readable } = require('stream');

describe('importDataHttp', () => {
    let originalEnv;

    beforeAll(() => {
        originalEnv = process.env.SCHEDULER_SECRET_TOKEN;
        process.env.SCHEDULER_SECRET_TOKEN = 'secret-token';

        // Require index.js to register the function
        require('./index.js');
    });

    afterAll(() => {
        process.env.SCHEDULER_SECRET_TOKEN = originalEnv;
    });

    beforeEach(() => {
        jest.clearAllMocks();
    });

    it('should return 401 if authorization header is missing', async () => {
        const req = { headers: {} };
        const res = {
            status: jest.fn().mockReturnThis(),
            send: jest.fn(),
        };

        await registeredHandler(req, res);

        expect(res.status).toHaveBeenCalledWith(401);
        expect(res.send).toHaveBeenCalledWith('Unauthorized');
    });

    it('should return 401 if authorization header is incorrect', async () => {
        const req = { headers: { authorization: 'Bearer WRONG_TOKEN' } };
        const res = {
            status: jest.fn().mockReturnThis(),
            send: jest.fn(),
        };

        await registeredHandler(req, res);

        expect(res.status).toHaveBeenCalledWith(401);
        expect(res.send).toHaveBeenCalledWith('Unauthorized');
    });

    it('should return 200 on successful authorization and execution', async () => {
        const req = { headers: { authorization: 'Bearer secret-token' } };
        const res = {
            status: jest.fn().mockReturnThis(),
            send: jest.fn(),
        };

        // Mock axios to return an empty stream, which will skip processing data
        const mockStream = new Readable({
            read() {
                this.push(null);
            }
        });
        axios.mockResolvedValue({ data: mockStream });

        await registeredHandler(req, res);

        expect(res.status).toHaveBeenCalledWith(200);
        expect(res.send).toHaveBeenCalledWith('Synchronization successful');
    });
});
