import org.apache.spark.sql.SparkSession
import java.util.Properties
import org.apache.spark.sql.functions._

object Transformation {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("DataFrame")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    val jdbcURL = "jdbc:postgresql://localhost:5432/mydb"

    val connectionProperties = new Properties()
    connectionProperties.put("user", "mahek")
    connectionProperties.put("password", "mahek")
    connectionProperties.put("driver", "org.postgresql.Driver")

    readfromPostgre(spark, jdbcURL, connectionProperties)
    performTransformation(spark, jdbcURL, connectionProperties)

    System.in.read()
    spark.stop()
  }

  def readfromPostgre(spark: SparkSession, jdbcURL: String, properties: Properties): Unit = {
    println("----------Read Table----------")
    val employeedf = spark.read
      .jdbc(jdbcURL, "employees", properties)
    employeedf.show()
  }

  def performTransformation (spark: SparkSession, jdbcURL:String, properties: Properties) : Unit = {
    import spark.implicits._
    val employeeDF = spark.read
      .jdbc(jdbcURL, "employees", properties)

    val categoriseSalary = employeeDF
      .withColumn("Salary_Category",
        when($"salary" > 6000, "High")
          .when($"salary" > 5000, "Medium")
          .otherwise("Low"))
    println("Employees with Salary Category")
    categoriseSalary.show()
    categoriseSalary.write
      .mode("overwrite")
      .jdbc(jdbcURL, "salary_category", properties)
    println("salary_category data successfully loaded")
  }

}

