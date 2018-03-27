package demo

import org.scalatest.FunSuite

class WordCountSpec extends FunSuite {

  WordCountActor.main(Array("2551"))
  WordCountActor.main(Array("2552"))
  WordCountActor.main(Array("2553"))
  val actorRef = WordCountActor.refs.head

  test("connectDB") {
    actorRef ! Count(Map("one" -> 2, "two" -> 3))
    actorRef ! Count(Map("four" -> 2, "three" -> 3))
    actorRef ! Count(Map("three" -> 2, "one" -> 3))
    // Expect console
    // (one,5)
    // (three,5)
    // (four,2)
    // (two,3)

  }
}
