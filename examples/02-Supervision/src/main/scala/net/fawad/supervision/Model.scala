package net.fawad.supervision

case class Find(needle: Int, haystack: List[Int])

case class FindResult(req: Find, found: Boolean)

case class Crashed(msg:Any)