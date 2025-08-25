db = db.getSiblingDB('chatapp');

if (!db.roles.find().count()) {
    db.createCollection('roles');
    db.roles.insertMany([
        { name: "USER"},
        { name: "ADMIN"}
    ]);
    db.roles.createIndex({ "name": 1 }, { unique: true });
    print('Roles collection initialized successfully');
}