package bigtop
package core

import org.scalatest._

class UrlSuite extends FunSuite with Assertions {
  
  def testUrl(urlString: String)(urlStruct: Url) = {
    test("encode/decode " + urlString) {
      expect(urlString)(urlStruct.urlString)
      expect(Some(urlStruct))(Url.fromString(urlString))
      
      urlStruct.toJavaURL.foreach { javaURL =>
        expect(urlStruct)(Url.fromJavaURL(javaURL))
      }
    }
  }
  
  // Stolen from the unit tests for net/url in Racket:
  
  testUrl("")(
    Url(None, None, None, None, false, Nil, Nil, None))

  testUrl("/")(
    Url(None, None, None, None, true, List(""), Nil, None))

  testUrl("/a/b/c")(
    Url(None, None, None, None, true, List("a", "b", "c"), Nil, None))

  testUrl("a/b/c")(
    Url(None, None, None, None, false, List("a", "b", "c"), Nil, None))

  testUrl("http:/")(
    Url(Some("http"), None, None, None, true, List(""), Nil, None))

  testUrl("http:///")(
    Url(Some("http"), None, Some(""), None, true, List(""), Nil, None))

  testUrl("http://www.racket-lang.org")(
    Url(Some("http"), None, Some("www.racket-lang.org"), None, false, Nil, Nil, None))

  testUrl("http://www.racket-lang.org/")(
    Url(Some("http"), None, Some("www.racket-lang.org"), None, true, List(""), Nil, None))

  testUrl("http://www.racket-lang.org/a/b/c")(
    Url(Some("http"), None, Some("www.racket-lang.org"), None, true, List("a", "b", "c"), Nil, None))

  testUrl("http://robby@www.racket-lang.org/a/b/c")(
    Url(Some("http"), Some("robby"), Some("www.racket-lang.org"), None, true, List("a", "b", "c"), Nil, None))

  testUrl("http://www.racket-lang.org:8080/a/b/c")(
    Url(Some("http"), None, Some("www.racket-lang.org"), Some(8080), true, List("a", "b", "c"), Nil, None))

  testUrl("http://www.racket-lang.org/a/b/c#joe")(
    Url(Some("http"), None, Some("www.racket-lang.org"), None, true, List("a", "b", "c"), Nil, Some("joe")))

  testUrl("http://www.racket-lang.org/a/b/c?tim=")(
    Url(Some("http"), None, Some("www.racket-lang.org"), None, true, List("a", "b", "c"), List("tim" -> ""), None))

  testUrl("http://www.racket-lang.org/a/b/c?tim=#joe")(
    Url(Some("http"), None, Some("www.racket-lang.org"), None, true, List("a", "b", "c"), List("tim" -> ""), Some("joe")))

  testUrl("http://www.racket-lang.org/a/b/c?tim=tim#joe")(
    Url(Some("http"), None, Some("www.racket-lang.org"), None, true, List("a", "b", "c"), List("tim" -> "tim"), Some("joe")))

  testUrl("http://www.racket-lang.org/a/b/c?tam=tom#joe")(
    Url(Some("http"), None, Some("www.racket-lang.org"), None, true, List("a", "b", "c"), List("tam" -> "tom"), Some("joe")))

  testUrl("http://www.racket-lang.org/a/b/c?tam=tom&pam=pom#joe")(
    Url(Some("http"), None, Some("www.racket-lang.org"), None, true, List("a", "b", "c"), List("tam" -> "tom", "pam" -> "pom"), Some("joe")))

  // Note: This behaviour differs from Racket, which encodes the " " as "%20":
  testUrl("http://www.racket-lang.org/a/b/c?ti%23m=tom#jo+e")(
    Url(Some("http"), None, Some("www.racket-lang.org"), None, true, List("a", "b", "c"), List("ti#m" -> "tom"), Some("jo e")))

  // Note: This behaviour differs from Racket, which encodes the " " as "%20":
  testUrl("http://www.racket-lang.org/+a+/+b+/+c+")(
    Url(Some("http"), None, Some("www.racket-lang.org"), None, true, List(" a ", " b ", " c "), Nil, None))

  // Note: This behaviour differs from Racket, which encodes the " " as "%20":
  testUrl("http://robb+y@www.racket-lang.org")(
    Url(Some("http"), Some("robb y"), Some("www.racket-lang.org"), None, false, Nil, Nil, None))

  // Note: This behaviour differs from Racket, which encodes the " " as "%20":
  testUrl("http://robb+y@www.racket-lang.org/")(
    Url(Some("http"), Some("robb y"), Some("www.racket-lang.org"), None, true, List(""), Nil, None))

  testUrl("http://www.racket-lang.org/%25a/b%2F/c")(
    Url(Some("http"), None, Some("www.racket-lang.org"), None, true, List("%a", "b/", "c"), Nil, None))

  testUrl("http://robby:password@www.racket-lang.org/")(
    Url(Some("http"), Some("robby:password"), Some("www.racket-lang.org"), None, true, List(""), Nil, None))

  testUrl("/foo/bar?a=b&c=d#e")(
    Url(None, None, None, None, true, List("foo", "bar"), List("a" -> "b", "c" -> "d"), Some("e")))

  testUrl("http://www.drscheme.org/a%3A%40%21%24%26%27%28%29*%2B%2C%3Dz/%2F%3F%23%5B%5D%3B/")(
    Url(Some("http"), None, Some("www.drscheme.org"), None, true, List("a:@!$&'()*+,=z", "/?#[];", ""), Nil, None))
    
  // FIXME: URL encoding/decoding does not distinguish between "%2E" and "." or "%2E%2E" and ".."
  testUrl("http://www.drscheme.org/./../.../abc.def")(
    Url(Some("http"), None, Some("www.drscheme.org"), None, true, List(".", "..", "...", "abc.def"), Nil, None))
    
  test("Url.toString(String)") {
    expect("http://www.racket-lang.org/a/b/c?tam=tom;pam=pom#joe")(
      Url(Some("http"), None, Some("www.racket-lang.org"), None, true, List("a", "b", "c"), List("tam" -> "tom", "pam" -> "pom"), Some("joe")).urlString(";"))

    expect("http://www.racket-lang.org/a/b/c?tam=tom&pam=pom#joe")(
      Url(Some("http"), None, Some("www.racket-lang.org"), None, true, List("a", "b", "c"), List("tam" -> "tom", "pam" -> "pom"), Some("joe")).urlString("&"))
  }
  
  test("get, getOne") {
    val url = Url.fromString("http://untyped.com?a=b&c=d&a=f").get
    
    expect(Some("b"))(url.getOne("a"))
    expect(List("b", "f"))(url.get("a"))
    
    expect(None)(url.getOne("b"))
    expect(Nil)(url.get("b"))
    
    expect(Some("d"))(url.getOne("c"))
    expect(List("d"))(url.get("c"))
  }
  
  test("replace") {
    val url = Url.fromString("http://untyped.com?a=b&c=d").get
    expect(Url.fromString("http://untyped.com?c=d&a=c").get)(url.set("a", "c"))
    expect(Url.fromString("http://untyped.com?c=d&a=c&a=d").get)(url.set("a", "c", "d"))

    val url2 = Url.fromString("http://untyped.com?a=b&a=c").get
    expect(Url.fromString("http://untyped.com?a=d").get)(url2.set("a", "d"))
  }
  
  test("remove") {
    val url = Url.fromString("http://untyped.com?a=b&c=d&a=e").get
    expect(Url.fromString("http://untyped.com?c=d").get)(url.remove("a"))
  }
  
  test("update path and pathAbsolute") {
    val abs = Url.fromString("http://example.com/").get 
    val rel = Url.fromString("http://example.com").get
    
    assert(abs.pathAbsolute === true)
    assert(rel.pathAbsolute === false)

    assert(abs.path === List(""))
    assert(rel.path === Nil)
    
    assert(abs.path(Nil).pathAbsolute === false)
    assert(rel.path(List("")).pathAbsolute === true)
    
    assert(abs.host(None).pathAbsolute === true)
    assert(rel.host(None).pathAbsolute === false)
    
    val abs2 = Url.fromString("/a/b/c").get
    val rel2 = Url.fromString("a/b/c").get
    
    assert(abs2.pathAbsolute === true)
    assert(rel2.pathAbsolute === false)

    assert(abs2.path === List("a", "b", "c"))
    assert(rel2.path === List("a", "b", "c"))
    
    assert(abs2.path(List("a", "b", "c")).pathAbsolute === true)
    assert(rel2.path(List("a", "b", "c")).pathAbsolute === false)
    
    assert(abs2.host(Some("example.com")).pathAbsolute === true)
    assert(rel2.host(Some("example.com")).pathAbsolute === true)
  }
  
}