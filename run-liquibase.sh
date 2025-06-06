
#primitave for now
mvn liquibase:clearCheckSums  \
  -Dliquibase.url=jdbc:postgresql://localhost:6543/postgres \
  -Dliquibase.username=postgres \
  -Dliquibase.password=docker \
  -Dliquibase.driver=org.postgresql.Driver
