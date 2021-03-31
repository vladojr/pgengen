# PgEnGen (Postgres Entity Generator)

Generates java DAO classes for postgres tables with SQL access syntax.\
Objects are identified using their ctid row identifier rather than their primary key.

## Usage

### Installation

Run following script on target database.

```
drop schema if exists pgengen cascade;

create schema pgengen;

create or replace function pgengen.check_tx(txid bigint) returns boolean language plpgsql as $$
begin
if txid is distinct from txid_current() then
	raise exception 'txid check failed! expected %, got %', txid_current(), txid
	 using hint = 'Make sure updates and deletes are made within the same transaction where the object is retrieved.';
end if;
return true;
end $$;
```

### Read

```java
try(final Connection conn=Db.app(Db.READ_ONLY,Db.MANUAL_COMMIT)){
//read first record
final Contact oneContact=Contact.first(conn,"where id=?",100);
	if(oneContact==null)throw new Exception("Contact with id 100 not found!");

//read all records
final List<Contact> allContacts=Contact.all(conn,"where name ilike ? order by name limit 100","john");
	if(allContacts.isEmpty())throw new Exception("No contact with name john!");

	//iterate over results without fetching all at once
	Contact.each(conn,(final int row,final Contact contact)->{
	System.out.println("contact first name "+contact.fname);
	},"where name ilike ?","john");

	conn.commit();
	}
```

### Update / Delete

It is good practice to lock the row during retrieval before modifying it if we know which row will be modified in
advance.\
The lock is acquired using the "for update of << table >>" syntax.\
Always only lock specific rows to be updated, otherwise you may encounter deadlocks.

```java
try(final Connection conn=Db.app(Db.READ_WRITE,Db.MANUAL_COMMIT)){
final Contact contact=new Contact(0,0,0,0l,"cpid","fname","lname","email","phone","role",'A',"title","fax");
	contact.insert(conn,
	Contact.Columns.id.DEFAULT(),
	Contact.Columns.cid.expression("nextval('cid')"),
	Contact.Columns.fname.CURRENT_TIMESTAMP(),
	Contact.Columns.cpid.omit());
	conn.commit();
	}

	try(final Connection conn=Db.app(Db.READ_WRITE,Db.MANUAL_COMMIT)){
final Contact contact=Contact.first(conn,"where fname=? for update of contact","fname");
	contact.fname="FNAME";
	contact.updateByCtid(conn);
	conn.commit();
	}

	try(final Connection conn=Db.app(Db.READ_WRITE,Db.MANUAL_COMMIT)){
final Contact contact=Contact.first(conn,"where fname=? for update of contact","FNAME");
	contact.deleteByCtid(conn);
	conn.commit();
	}
```

## Generating classes

Java usage example

```java
final Path destination=Paths.get("src-gen/db/");
	Files.createDirectories(destination);

	try(final Connection conn=Db.app(READ_ONLY,AUTO_COMMIT)){
final PgEnGen generator=new PgEnGen("schema",".*","db",destination);
	generator.generateTables(conn);
	//generator.generateEnums(conn);
	}
```

Gradle integration

```groovy
task generateDbMappings(type: JavaExec) {
    group = "generators"
    description "Generates new entity classes from database"
    classpath = sourceSets.main.runtimeClasspath
    main = 'sk.prosoft.epoukaz2.db.DbMappingsGenerator'
}
```

## Limitations

Retrieved objects are only updatable/deletable within the same transaction where they were fetched from the database.

```java
final Contact contact1,contact2;
	try(final Connection conn=Db.app(Db.READ_WRITE,Db.MANUAL_COMMIT)){
	contact1=Contact.first(conn,"where fname=? for update of contact","FNAME");
	contact2=Contact.first(conn,"where fname=? for update of contact","FNAME");
	contact1.updateByCtid(conn);
	contact2.updateByCtid(conn);//throws exception because ctid has already changed and the row could not be updated!
	}

	try(final Connection conn=Db.app(Db.READ_WRITE,Db.MANUAL_COMMIT)){
	contact1.updateByCtid(conn);//throws exception because txid has already changed and ctid may point to a different row!
	}
```

### TODO

## toString

## clone

## weak query cache