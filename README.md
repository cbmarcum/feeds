# Feeds Plugin

A plugin that renders RSS/Atom feeds, or any other formats supported by the ROME API, as well as iTunes compatible podcasts.

To install, add a dependency in your BuildConfig.groovy:

     plugins {
        compile ':feeds:1.6'
     }

It works like this - you call render() method from your controller action as normal, but instead
of using a view you specify a feedType parameter and a feedVersion parameter, as well as a closure that uses a custom feed builder to define the feed. Currently, feed types "rss" and "atom" are supported.

> If you are using Safari (certainly version 3) you may find that links to your feeds are rejected with a message about it not being able to get feeds from localhost. The solution is to change your link in your address bar to your local host name i.e. "http://yourmacbook.local:8080/yourfeedtest/feed" and from then on the links will work.


## Using the dynamic render method and the Feed Builder DSL


     class YourController implements grails.plugins.feeds.FeedRenderer {
        def feed = {
           render(feedType:"rss", feedVersion:"2.0") {
              title = "My test feed"
              link = "http://your.test.server/yourController/feed"
              description = "The funky Grails news feed"
     
              Article.list().each { article ->
                      entry(article.title) {
                    link = "http://your.test.server/article/${article.id}"
                    article.content // return the content
                 }
              }
           }
        }
     }


The *feedType* parameter is required. The *feedVersion* is optional and will default to 2.0 for RSS and 1.0 for Atom if not supplied. Current tested feedType values are "rss" and "atom", but any ROME supported feed type should work.

As of version 1.4 the builder has predefined node types:

* entry - an entry in the feed
* content - content of an entry in the feed
* enclosure - an attachment to an entry (used for podcasts)
* iTunes - special iTunes Music Store podcast tags at feed or entry level

> The different feed types have different requirements. For example RSS 2.0 requires a "description" property in the channel (root node of the builder) whereas older versions may not. So watch out for errors relating to properties being required in certain nodes.

## Commercial Support

Commercial support is available for this and other [Grailsrocks plugins|http://grailsrocks.com].

## Builder semantics

Some of the feed formats have different required properties that you must set on the feed (AKA Channel)
and the child nodes (entry and content). Exceptions will occur if you don't meet these constraints.

The feed builder is very forgiving. The general pattern is:

* set feed top-level properties i.e. link and title
* define entry nodes and properties of them
* define content for entry nodes, and properties of content

There are some smarts, namely:

* entry nodes can take a title parameter as a shortcut.
* content nodes can take a string parameter which is used as the text/plain content body
* entry node bodies can just return an object, the string value of which will be used as text/plain content for the entry
* entry and content nodes can take a map as parameter, to set any properties of the node

Common properties of entry nodes you may want to set:

* `publishedDate` - the date the entry was published
* `categories` - the list of categories
* `author` - author name
* `link` - link to the online entry


Since the entry node is based on the Interface SyndEntry you can use all field provided by it, see [SyndEntryAPI|https://rome.dev.java.net/apidocs/1_0/com/sun/syndication/feed/synd/SyndEntry.html]

Common properties of content nodes you may want to set:

* `type` -the mime type of the content

Descriptions are currently not directly supported by the builder but can be constructed using the ROME API directly.

## Examples

### Setting root node (feed) properties directly using FeedBuilder. 


> This section does not apply if using the render(feedType:"xxxx") mechanism, only if using the FeedBuilder directly, in the render(feedType:"xxx") case you can only set feed properties in the top level of the closure


     def builder = new FeedBuilder()
     builder.feed {
        title = "My title"
        link = "http://www.myblogsite.com"    
     }

     //setting the optional RSS image tag
     def builderWithImage = new FeedBuilder()
     builderWithImage.feed {
        title = "My title"
        link = "http://www.myblogsite.com" 
        //import com.sun.syndication.feed.synd.SyndImageImpl
        image = new SyndImageImpl(url:'http://', title:'My feed', link:'http://mysite.de')
     }
     
     def feedA = builder.makeFeed('rss_2.0')
     
     def builder2 = new FeedBuilder()
     builder2.feed(title: "My title", link: "http://www.myblogsite.com") {
       // nodes here     
     }
     def feedB = builder2.makeFeed('rss_2.0')

     def builder3 = new FeedBuilder()
     builder3.feed("My title") {
        link = "http://www.myblogsite.com"    
     }
     def feedC = builder2.makeFeed('rss_2.0')


### Entry nodes


     entry {
        title = "Article 1"
        link = "http://somedomain.com/feed/1"
        publishedDate = new Date()
    
        //content here
     }

     entry("Article 2") {
        link = "http://somedomain.com/feed/2"
        publishedDate = new Date()
        //content here
     }

     entry(title:"Title here", link:"http://somedomain.com/feed") {
        publishedDate = new Date()
        //content here
     }


### Content nodes 

     content() {
        type = "text/html"
        '<p>Hello world</p>' // can use "return" also
     }

     content('Hello world') 

     content(type:'text/html') {
        '<p>Hello world</p>' // can use "return" also
     }

     content(type:'text/html', value:'<p>Hello world</p>')


### enclosure nodes 

To include an "embedded" resource such as an mp3 or video file, you use enclosure nodes that produce the equivalent enclosure concept in the feed that you produce.

The nodes have *type*, *url* and *length* properties, which should be self explanatory:


     entry {
        title = article.title
        link = g.createLink( controller:'podcast', action:'index', id:article.name, absolute: true))
        enclosure {
           type = 'audio/mp3'
           url = g.createLinkTo(dir:"podcast/episode${meta.episodeNum}", 
              file:"episode${meta.episodeNum}.mp3", absolute: true)
           length = 9999
        }
     }

### iTunes nodes 

These may be placed at the top level of the builder to set feed-specific tags, or under an entry to provide per-episode tags. You can set any properties that are valid for the entry or feed, as per the FeedInformation and EntryInformation interfaces defined in the iTunes module for ROME, javadocs of which are at [https://rome.dev.java.net/nonav/apidocs/subprojects/modules/itunes/0.4/apidocs/]


     render(feedType:"rss", feedVersion:"2.0") {
        title = FEED_TITLE
        description = FEED_DESCRIPTION
        link = g.createLink(controller:'podcast', action:'index', absolute: true)
        iTunes {
           summary = FEED_DESCRIPTION
           keywords = FEED_KEYWORDS
              categories = [ "Comedy" ]
              image = new URL(some_img_url_for_cover_art_600x600)
              author = FEED_AUTHOR
              subtitle = FEED_SUBTITLE
              explicit = true 
              ownerName = "Yournamel"
              ownerEmailAddress = "You at some .com"
           }
           articles.each { article ->
              def meta = article.meta
     
              entry {
                 title = article.title
                 link = g.createLink( controller:'podcast', action:'index', id:article.name, absolute: true))
                 enclosure(type: 'audio/mp3', 
                           url: g.createLinkTo(dir:"podcast/episode${meta.episodeNum}", 
                                  file:"episode${meta.episodeNum}.mp3", absolute: true), 
                           length: 0 /* work out the length of your file here */)
                    publishedDate = article.publishDate

                    // do the itunes Meta
                    iTunes {
                       author = FEED_AUTHOR
                       summary = article.content
                       durationText = meta.duration
                       keywords = ['fun', 'grails', 'rocks']
                       explicit = true
                    }

                    content(article.content)
                 }
              }
           }
        }
     }


See [http://www.apple.com/itunes/store/podcaststechspecs.html#rss] for the detailed information about how this information is used within the iTunes music store podcast directory.

The above will also generate a non-iTunes specific podcast i.e. an RSS feed with enclosures.

### Using the taglib

There is a "<feed:meta>" tag provided to generate the <link> tag correctly for use within the <head> section of your content. It sets the mime type and title of the link correctly.


     <feed:meta kind="rss" version="2.0" controller="feed" action="index"/>
     <feed:meta kind="atom" version="1.0" controller="feed" action="index"/>


