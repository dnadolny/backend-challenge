import com.dnadolny.ada.app._
import com.dnadolny.ada._
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    val dbCredentials = DbCredentials(s"jdbc:postgresql://${env("DB_HOST")}/postgres", "postgres", env("DB_PASSWORD"))
    context.mount(new ConversationAPIServlet(new RealClock, new Db(dbCredentials)), "/*")
  }
  
  def env(name: String) = Option(System.getenv(name)).getOrElse(throw new Exception(s"Expected environment variable $name was not found"))
}
