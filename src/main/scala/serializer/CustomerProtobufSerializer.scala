package serializer

import akka.serialization.SerializerWithStringManifest
import worker.model.events._

class CustomerProtobufSerializer extends SerializerWithStringManifest {

  override def identifier: Int = 901110116

  override def manifest(o: AnyRef): String = o.getClass.getName

  final val WorkAcceptedManifest = classOf[WorkAccepted].getName
  final val WorkCompletedManifest = classOf[WorkCompleted].getName
  final val WorkerFailedManifest = classOf[WorkerFailed].getName
  final val WorkerTimedOutManifest = classOf[WorkerTimedOut].getName
  final val WorkStartedManifest = classOf[WorkStarted].getName

  override def toBinary(o: AnyRef): Array[Byte] = {
    println("inside toBinary")
    o match {
      case a: WorkAccepted => a.toByteArray
      case c: WorkCompleted => c.toByteArray
      case f: WorkerFailed => f.toByteArray
      case t: WorkerTimedOut => t.toByteArray
      case s: WorkStarted => s.toByteArray
    }
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = {

    println("inside fromBinary" + manifest)

    manifest match {
      case WorkAcceptedManifest => WorkAccepted.parseFrom(bytes)
      case WorkCompletedManifest => WorkCompleted.parseFrom(bytes)
      case WorkerFailedManifest => WorkerFailed.parseFrom(bytes)
      case WorkerTimedOutManifest => WorkerTimedOut.parseFrom(bytes)
      case WorkStartedManifest => WorkStarted.parseFrom(bytes)
    }
  }
}
