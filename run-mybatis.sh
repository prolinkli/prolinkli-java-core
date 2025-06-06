mvn mybatis-generator:generate \
  -Djdbc.driverClass=org.postgresql.Driver \
  -Djdbc.url=jdbc:postgresql://localhost:6543/postgres \
  -Djdbc.userId=postgres \
  -Djdbc.password=docker
