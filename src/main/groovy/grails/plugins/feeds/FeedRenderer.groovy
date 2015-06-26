package grails.plugins.feeds

import grails.artefact.*
import groovy.transform.*
import grails.web.api.*
import org.grails.core.artefact.ControllerArtefactHandler
import com.sun.syndication.io.SyndFeedOutput

@Enhances("Controller")
trait FeedRenderer extends grails.artefact.controller.support.ResponseRenderer {

	static MIME_TYPES = [
		atom:'application/atom+xml',
		rss:'application/rss+xml'
	]	

	void render(Map params, Closure closure) {
		if (params.feedType) {
			// Here we should assert feed type is supported
			def builder = new FeedBuilder()
			builder.feed(closure)

			def type = params.feedType
			def mimeType = params.contentType ?: MIME_TYPES[type]
			if (!mimeType) {
				throw new IllegalArgumentException("No mime type known for feed type [${type}]")
			}

			response.contentType = mimeType
			response.characterEncoding = "UTF-8"

			new SyndFeedOutput().output(builder.makeFeed(type, params.feedVersion),response.writer)
		}
		else {
			// Defer to original render method
			super.render(params, closure)
		}
	}
}