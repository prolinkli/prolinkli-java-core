--liquibase formatted sql
--changeset kerdogan:0001.CreateTableProcedure runAlways:true splitStatements:false

-- Enhanced table creation function with smart defaults and flexibility
CREATE OR REPLACE FUNCTION create_table(
    table_name TEXT,
    columns TEXT DEFAULT '',
    options JSONB DEFAULT '{}'::JSONB
) RETURNS TEXT AS $
DECLARE
    sql_statement TEXT;
    schema_name TEXT := COALESCE(options->>'schema', 'public');
    add_id BOOLEAN := COALESCE((options->>'add_id')::BOOLEAN, true);
    add_timestamps BOOLEAN := COALESCE((options->>'add_timestamps')::BOOLEAN, true);
    add_soft_delete BOOLEAN := COALESCE((options->>'add_soft_delete')::BOOLEAN, false);
    primary_key TEXT := options->>'primary_key';
    indexes TEXT[] := ARRAY(SELECT jsonb_array_elements_text(options->'indexes'));
    unique_constraints TEXT[] := ARRAY(SELECT jsonb_array_elements_text(options->'unique_constraints'));
    foreign_keys JSONB := options->'foreign_keys';
    table_comment TEXT := options->>'comment';
    if_not_exists BOOLEAN := COALESCE((options->>'if_not_exists')::BOOLEAN, true);
    temp_table BOOLEAN := COALESCE((options->>'temporary')::BOOLEAN, false);
    i INT;
    fk JSONB;
    full_table_name TEXT;
BEGIN
    -- Validate inputs
    IF table_name IS NULL OR trim(table_name) = '' THEN
        RAISE EXCEPTION 'Table name cannot be empty';
    END IF;
    
    -- Allow empty columns if we're adding default fields
    IF columns IS NULL THEN
        columns := '';
    END IF;
    
    -- Build full table name
    full_table_name := CASE 
        WHEN schema_name != 'public' THEN schema_name || '.' || table_name
        ELSE table_name
    END;
    
    -- Start building the SQL statement
    sql_statement := 'CREATE ';
    
    -- Add TEMPORARY if specified
    IF temp_table THEN
        sql_statement := sql_statement || 'TEMPORARY ';
    END IF;
    
    sql_statement := sql_statement || 'TABLE ';
    
    -- Add IF NOT EXISTS if specified
    IF if_not_exists THEN
        sql_statement := sql_statement || 'IF NOT EXISTS ';
    END IF;
    
    sql_statement := sql_statement || full_table_name || ' (';
    
    -- Add auto-generated ID column if requested
    IF add_id AND primary_key IS NULL THEN
        sql_statement := sql_statement || E'\n    id BIGSERIAL PRIMARY KEY,';
    END IF;
    
    -- Add user-defined columns (if any)
    IF trim(columns) != '' THEN
        sql_statement := sql_statement || E'\n    ' || columns;
        
        -- Add comma if we have more fields coming
        IF add_timestamps OR add_soft_delete OR primary_key IS NOT NULL THEN
            sql_statement := sql_statement || ',';
        END IF;
    END IF;
    
    -- Add timestamps if requested
    IF add_timestamps THEN
        -- Add comma if we had user columns
        IF trim(columns) != '' AND NOT (add_soft_delete OR primary_key IS NOT NULL) THEN
            sql_statement := sql_statement || E',\n    created_at TIMESTAMPTZ DEFAULT NOW(),';
        ELSE
            sql_statement := sql_statement || E'\n    created_at TIMESTAMPTZ DEFAULT NOW(),';
        END IF;
        sql_statement := sql_statement || E'\n    updated_at TIMESTAMPTZ DEFAULT NOW()';
    END IF;
    
    -- Add soft delete column if requested
    IF add_soft_delete THEN
        sql_statement := sql_statement || E',\n    deleted_at TIMESTAMPTZ NULL';
    END IF;
    
    -- Add custom primary key if specified
    IF primary_key IS NOT NULL THEN
        sql_statement := sql_statement || E',\n    PRIMARY KEY (' || primary_key || ')';
    END IF;
    
    -- Add unique constraints
    IF array_length(unique_constraints, 1) > 0 THEN
        FOR i IN 1..array_length(unique_constraints, 1) LOOP
            sql_statement := sql_statement || E',\n    UNIQUE (' || unique_constraints[i] || ')';
        END LOOP;
    END IF;
    
    -- Add foreign key constraints
    IF foreign_keys IS NOT NULL THEN
        FOR fk IN SELECT * FROM jsonb_array_elements(foreign_keys) LOOP
            sql_statement := sql_statement || E',\n    FOREIGN KEY (' || 
                           (fk->>'column') || ') REFERENCES ' || 
                           (fk->>'references') || 
                           CASE WHEN fk->>'on_delete' IS NOT NULL 
                                THEN ' ON DELETE ' || (fk->>'on_delete')
                                ELSE '' END ||
                           CASE WHEN fk->>'on_update' IS NOT NULL 
                                THEN ' ON UPDATE ' || (fk->>'on_update')
                                ELSE '' END;
        END LOOP;
    END IF;
    
    sql_statement := sql_statement || E'\n);';
    
    -- Add table comment if specified
    IF table_comment IS NOT NULL THEN
        sql_statement := sql_statement || E'\n\nCOMMENT ON TABLE ' || full_table_name || 
                        ' IS ' || quote_literal(table_comment) || ';';
    END IF;
    
    -- Execute the CREATE TABLE statement
    EXECUTE sql_statement;
    
    -- Create indexes if specified
    IF array_length(indexes, 1) > 0 THEN
        FOR i IN 1..array_length(indexes, 1) LOOP
            EXECUTE format('CREATE INDEX IF NOT EXISTS idx_%s_%s ON %s (%s)',
                          table_name, 
                          replace(replace(indexes[i], ' ', '_'), ',', '_'),
                          full_table_name,
                          indexes[i]);
        END LOOP;
    END IF;
    
    -- Create updated_at trigger if timestamps are enabled
    IF add_timestamps THEN
        -- Create the trigger function if it doesn't exist
        EXECUTE '
        CREATE OR REPLACE FUNCTION update_updated_at_column()
        RETURNS TRIGGER AS $trigger$
        BEGIN
            NEW.updated_at = NOW();
            RETURN NEW;
        END;
        $trigger$ language plpgsql;';
        
        -- Create the trigger
        EXECUTE format('
        CREATE TRIGGER update_%s_updated_at 
        BEFORE UPDATE ON %s 
        FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();',
        table_name, full_table_name);
    END IF;
    
    RETURN format('Table %s created successfully with %s columns', 
                  full_table_name, 
                  CASE WHEN add_id THEN 'auto-generated ' ELSE '' END ||
                  CASE WHEN add_timestamps THEN 'timestamp ' ELSE '' END ||
                  'custom');
    
EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Error creating table %: %', full_table_name, SQLERRM;
END;
$$ LANGUAGE plpgsql;-- Helper function for quick table creation with just name and columns
CREATE OR REPLACE FUNCTION create_dry_table(
    table_name TEXT,
    columns TEXT
) RETURNS TEXT AS $$
BEGIN
    RETURN create_table_seamless(table_name, columns);
END;
$$ LANGUAGE plpgsql;


