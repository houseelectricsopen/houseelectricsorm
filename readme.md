##House Electrics ORM

This is an implementation of an object relational mapping for sqlite on android.

This is an implementation of an object relational mapping for sqlite on android.
It contains a minimal set of features to map a java object model to a relational database in an android app but nevertheless places a low burden on the developer.

Here are some of the features:
+ No database ids in domain objects – sqlite rowid is mapped internally to java domain object instances.
+ List Support
+ Polymorphic References
+ Customizeable type to db field mapping
+ Raw query s
+ Lazy Loading

See the [user guide](docs/userguide.html) for more detail