package grails.plugin.feeds

class TestController {

	def test1() {
		def articles = ['A', 'B', 'C']

		render(feedType:"rss", feedVersion:"2.0") {
			title = 'Test feed'
			link = 'http://somewhere.com/'
			description = "This is a test feed" 

			for (article in articles) {
				entry("Title for \$article") {
					"Content for \$article"
				}
			}
		}
	}

	def test2(String version) {
		def articles = ['A', 'B', 'C']

		render(feedType:"atom", feedVersion:"$version") {
			title = 'Test feed'
			link = 'http://somewhere.com/'
			description = "This is a test feed" 

			for (article in articles) {
				entry("Title for \$article") {
					content { 
						"Content for \$article"
					}
				}
			}
		}
	}

	def test3() {
		def articles = ['A', 'B', 'C']

		render(feedType:"rss", feedVersion:"2.0") {
			title = 'Test feed'
			link = 'http://somewhere.com/'
			description = "This is a test feed" 

			for (article in articles) {
				entry("Title for \$article") {
					content(type:'text/html') {
						return "Content for \$article"
					}

					link = 'http://somewhere.com/x'

					publishedDate = new Date()
				}
			}
		}
	}

	def test4() {
		def articles = ['A', 'B', 'C']

		render(feedType:"rss", feedVersion:"2.0") {
			title = 'Test feed'
			link = 'http://somewhere.com/'
			description = "This is a test feed" 

			for (article in articles) {
				entry("Title for \$article") {
					link = 'http://somewhere.com/x'

					publishedDate = new Date()

					content(type:'text/html') {
						return "Content for \$article"
					}
				}
			}
		}
	}

	def test5() {
		def articles = ['A', 'B', 'C']

		render(feedType:"rss", feedVersion:"2.0") {
			title = 'Test feed'
			link = 'http://somewhere.com/'
			description = "This is a test feed" 

			for (article in articles) {
				entry("Title for \$article") {
					link = 'http://somewhere.com/' + g.somethingFake.someFakeMetho('x')
					publishedDate = new Date()

					content(type:'text/html') {
						return "Content for \$article"
					}
				}
			}
		}
	}

	def test6() {
		render("Hello world")
	}

	def test7() {
		render {
			html {
				body {
				}
			}
		}
	}

	def test8() {
		render(contentType:"text/xml") {
			root {
				child {
				}
			}
		}
	}

	def test9() {
		render(feedType:"rss", feedVersion:"2.0") {
			title = "My test feed"
			link = "http://your.test.server/yourController/feed"
			description = "test"
		}
	}
}
