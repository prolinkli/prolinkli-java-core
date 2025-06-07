# MyBatis DAO Factory Integration

This document explains how to use MyBatis generated criteria with the generic DAO pattern and lazy-loading factory system.

## Architecture Overview

The system consists of several key components:

1. **Generic DAO** (`Dao<T, PK>`) - Provides CRUD operations with MyBatis integration
2. **DAO Factory** (`DaoFactory`) - Lazy-loaded static access to DAO instances
3. **Generated MyBatis Classes** - Type-safe criteria and mappers
4. **Service Layer** - Business logic using DAOs with criteria queries

## Key Features

- **Type-Safe Queries**: Use MyBatis generated Example classes for criteria
- **Lazy Loading**: DAOs are created and cached on first access
- **Static Access**: Access DAOs from any component without dependency injection
- **Automatic Integration**: Seamless integration with MyBatis mappers
- **Comprehensive CRUD**: Full CRUD operations with criteria support

## Usage Examples

### 1. Basic DAO Access

```java
// Get DAO instance (lazy-loaded and cached)
Dao<BuildInfoDb, BuildInfoDbKey> dao = DaoFactory.getBuildInfoDao();

// Or for any entity type
Dao<MyEntity, MyKey> customDao = DaoFactory.createDao(MyEntity.class, MyEntityMapper.class);
```

### 2. Simple Criteria Queries

```java
// Find by single criteria
BuildInfoDbExample example = new BuildInfoDbExample();
example.createCriteria().andVersionEqualTo("1.0.0");
List<BuildInfoDb> builds = dao.selectList(example);

// Find first match only
BuildInfoDb firstBuild = dao.select(example);
```

### 3. Complex Criteria Queries

```java
// Multiple criteria with AND conditions
BuildInfoDbExample example = new BuildInfoDbExample();
example.createCriteria()
    .andVersionEqualTo("1.0.0")
    .andEnvironmentEqualTo("production")
    .andBuildDateGreaterThan(new Date());

// Add ordering
example.setOrderByClause("build_date DESC");

List<BuildInfoDb> builds = dao.selectList(example);
```

### 4. OR Conditions

```java
BuildInfoDbExample example = new BuildInfoDbExample();

// First OR condition
example.createCriteria()
    .andVersionEqualTo("1.0.0")
    .andEnvironmentEqualTo("production");

// Second OR condition
example.or()
    .andVersionEqualTo("2.0.0")
    .andEnvironmentEqualTo("staging");

List<BuildInfoDb> builds = dao.selectList(example);
```

### 5. Pattern Matching

```java
BuildInfoDbExample example = new BuildInfoDbExample();
example.createCriteria()
    .andVersionLike("1.0%")  // Starts with "1.0"
    .andCommitHashNotLike("%temp%");  // Doesn't contain "temp"

List<BuildInfoDb> builds = dao.selectList(example);
```

### 6. Date Range Queries

```java
Date startDate = // ... your start date
Date endDate = // ... your end date

BuildInfoDbExample example = new BuildInfoDbExample();
example.createCriteria()
    .andBuildDateBetween(startDate, endDate)
    .andEnvironmentIn(Arrays.asList("production", "staging"));

List<BuildInfoDb> builds = dao.selectList(example);
```

### 7. Count Operations

```java
BuildInfoDbExample example = new BuildInfoDbExample();
example.createCriteria().andEnvironmentEqualTo("production");

long count = dao.count(example);
```

### 8. Delete with Criteria

```java
// Delete old builds
BuildInfoDbExample example = new BuildInfoDbExample();
example.createCriteria()
    .andBuildDateLessThan(cutoffDate)
    .andEnvironmentEqualTo("staging");

int deletedCount = dao.deleteByExample(example);
```

## Service Layer Integration

### Create a Service Class

```java
@Service
public class MyEntityService {
    
    private Dao<MyEntity, MyKey> getDao() {
        return DaoFactory.createDao(MyEntity.class, MyEntityMapper.class);
    }
    
    public List<MyEntity> findByStatus(String status) {
        MyEntityExample example = new MyEntityExample();
        example.createCriteria().andStatusEqualTo(status);
        return getDao().selectList(example);
    }
}
```

### Use from Controllers

```java
@RestController
public class MyController {
    
    @Autowired
    private MyEntityService service;
    
    @GetMapping("/entities/{status}")
    public List<MyEntity> getByStatus(@PathVariable String status) {
        return service.findByStatus(status);
    }
}
```

## Available Criteria Methods

For each database column, MyBatis generates these criteria methods:

- `andFieldEqualTo(value)` - Equals comparison
- `andFieldNotEqualTo(value)` - Not equals comparison
- `andFieldGreaterThan(value)` - Greater than comparison
- `andFieldGreaterThanOrEqualTo(value)` - Greater than or equal
- `andFieldLessThan(value)` - Less than comparison
- `andFieldLessThanOrEqualTo(value)` - Less than or equal
- `andFieldLike(pattern)` - SQL LIKE pattern matching
- `andFieldNotLike(pattern)` - SQL NOT LIKE pattern matching
- `andFieldIn(List<T> values)` - IN clause with list of values
- `andFieldNotIn(List<T> values)` - NOT IN clause
- `andFieldBetween(value1, value2)` - BETWEEN clause
- `andFieldNotBetween(value1, value2)` - NOT BETWEEN clause
- `andFieldIsNull()` - IS NULL check
- `andFieldIsNotNull()` - IS NOT NULL check

## Best Practices

### 1. Use Service Layer
Always wrap DAO calls in service methods for business logic:

```java
@Service
public class BuildInfoService {
    public List<BuildInfoDb> findProductionBuilds() {
        // Business logic here
        return DaoFactory.getBuildInfoDao().selectList(example);
    }
}
```

### 2. Handle Exceptions
Wrap DAO operations in try-catch blocks:

```java
public List<BuildInfoDb> findBuilds() {
    try {
        return dao.selectList(example);
    } catch (PersistenceException e) {
        logger.error("Failed to fetch builds", e);
        throw new ServiceException("Unable to fetch builds", e);
    }
}
```

### 3. Use Pagination for Large Results
For potentially large result sets, consider pagination:

```java
BuildInfoDbExample example = new BuildInfoDbExample();
example.createCriteria().andEnvironmentEqualTo("production");
example.setOrderByClause("build_date DESC LIMIT 100 OFFSET 0");
```

### 4. Optimize Queries
Use specific criteria to minimize database load:

```java
// Good: Specific criteria
example.createCriteria()
    .andEnvironmentEqualTo("production")
    .andBuildDateGreaterThan(lastWeek);

// Avoid: No criteria (fetches all records)
example = new BuildInfoDbExample();
```

## Adding New Entities

To add support for a new entity:

1. **Create the database table**
2. **Add to MyBatis Generator config**:
   ```xml
   <table tableName="my_table" 
          schema="public"
          domainObjectName="MyEntityDb" />
   ```
3. **Run the generator**: `./run-mybatis.sh`
4. **Add factory method**:
   ```java
   public static Dao<MyEntityDb, MyEntityDbKey> getMyEntityDao() {
       return getDao(MyEntityDb.class, MyEntityDbMapper.class);
   }
   ```

## Configuration

### Spring Configuration
Ensure the `DaoFactory` is scanned as a Spring component:

```java
@ComponentScan(basePackages = "com.prolinkli.framework.db.dao")
public class DatabaseConfig {
    // Your SqlSessionFactory bean configuration
}
```

### MyBatis Configuration
The system relies on the custom `DbExamplePlugin` to ensure generated Example classes extend `DbExample<T>`.

## Troubleshooting

### Common Issues

1. **DAO Factory not initialized**
   - Ensure `DaoFactory` is a Spring-managed bean
   - Check that `SqlSessionFactory` is properly configured

2. **Method not found errors**
   - Verify MyBatis generator has run successfully
   - Check that mapper interfaces are generated correctly

3. **ClassNotFoundException**
   - Ensure the generated classes are compiled
   - Run `./run-mybatis.sh` to regenerate if needed

### Debug Information

```java
// Check if factory is initialized
boolean initialized = DaoFactory.isInitialized();

// Check cache size
int cacheSize = DaoFactory.getCacheSize();

// Clear cache if needed (for testing)
DaoFactory.clearCache();
```

## Performance Considerations

- **DAO Caching**: DAO instances are cached for reuse
- **Connection Management**: Each operation opens/closes its own session
- **Criteria Optimization**: Use specific criteria to minimize database load
- **Batch Operations**: Use batch methods for multiple inserts/updates

## Thread Safety

- **DAO Factory**: Thread-safe with concurrent cache
- **DAO Instances**: Thread-safe (stateless)
- **MyBatis Sessions**: Each operation uses its own session

This integration provides a powerful, type-safe way to work with database operations while maintaining clean separation of concerns and easy accessibility from any component in your application. 