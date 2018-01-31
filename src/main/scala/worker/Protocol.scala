package worker

import spray.json.DefaultJsonProtocol
import worker.Frontend.Ok

trait Protocol extends DefaultJsonProtocol {

  implicit val okFormat = jsonFormat1(Ok)
}
