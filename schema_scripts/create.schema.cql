CREATE KEYSPACE test_migration_1 WITH REPLICATION = {
  'class': 'SimpleStrategy',
  'replication_factor': '1'
  --'Solr': '1'
};

USE test_migration_1;

-- SOME SIMPLE TEST STRUCTURE --
CREATE TABLE test_from ( 
  main_key uuid,
  name text,
  payload text,
  PRIMARY KEY ( main_key, name)  
);


CREATE TABLE test_to ( 
  main_key uuid,
  name text,
  new_field text,  
  payload text,
  PRIMARY KEY ( (main_key), new_field)  
);


-- dummy data:
INSERT INTO test_from (main_key,name,payload) VALUES (a896fad1-1a8a-4e33-94ba-01f56ed3bf19 , 'zosia', '{"zosia":{"someId":"dassa","OrderStatus":"Completed"}}'); 
INSERT INTO test_from (main_key,name,payload) VALUES (11223344-1a8a-4e33-94ba-01f56ed3bf19 , 'zosia', '{"zosia":{"someId":"12312","OrderStatus":"Completed"}}');
INSERT INTO test_from (main_key,name,payload) VALUES (11223344-1a8a-4e33-94ba-01f56ed3bf19 , 'krysia', '{"krysia":{"id":"12312"}}');


-- COPY OF REAL STRUCTURE --

------ FROM ------
CREATE TABLE entities ( 
  id text,
  user_id uuid,
  connection_id uuid,
  routing_key text,
  entity text,
  operation text,
  entity_name text,
  version text,
  platform text,  
  created_at timestamp,  
  original_meta text,
  payload text,
  PRIMARY KEY ( connection_id, entity, created_at, version, operation, id )  
);

------ TO ------
CREATE TABLE entities_generic ( 
  connection_id uuid,
  entity text,
  created_at_week text,
  version text,
  original_reference text,  
  created_at timestamp,  
  updated_at timestamp,
  platform text,  
  routing_key text,
  entity_name text,
  user_id uuid,  
  original_meta text,
  payload text,
  PRIMARY KEY ( (connection_id, entity, created_at_week), created_at, version, original_reference)  
);

CREATE TABLE entities_profile ( 
  connection_id uuid,
  version text,
  original_reference text,  
  created_at timestamp,
  updated_at timestamp,
  platform text,  
  routing_key text,  
  entity_name text,
  user_id uuid,
  original_meta text,
  payload text,
  PRIMARY KEY ( (connection_id), created_at, version, original_reference)  
);