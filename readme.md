##House Electrics ORM

This is an implementation of an object relational mapping for sqlite on android.

This is an implementation of an object relational mapping for sqlite on android.
It contains a minimum set of sufficient features to map a java object model to a relational database in an android app but nervertheless places a low burden on the developer.

Here are some of the features:
1. No database ids in domain objects – sqlite rowid is mapped internally to java domain object instances.
2. List Support
3. Polymorphic References
4. Customizeable type to db field mapping
5. Raw query s
6. Lazy Loading

See the [user guide](docs/userguide.html) for more detail