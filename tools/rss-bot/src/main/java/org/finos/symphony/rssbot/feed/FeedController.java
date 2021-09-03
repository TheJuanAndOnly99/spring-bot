package org.finos.symphony.rssbot.feed;

import java.util.Optional;

import org.finos.symphony.rssbot.alerter.FeedListCache;
import org.finos.symphony.rssbot.alerter.TimedAlerter;
import org.finos.symphony.rssbot.load.FeedLoader;
import org.finos.symphony.rssbot.notify.Notifier;
import org.finos.symphony.toolkit.workflow.annotations.ChatButton;
import org.finos.symphony.toolkit.workflow.annotations.ChatRequest;
import org.finos.symphony.toolkit.workflow.annotations.ChatResponseBody;
import org.finos.symphony.toolkit.workflow.annotations.ChatVariable;
import org.finos.symphony.toolkit.workflow.annotations.WorkMode;
import org.finos.symphony.toolkit.workflow.content.Addressable;
import org.finos.symphony.toolkit.workflow.content.Chat;
import org.finos.symphony.toolkit.workflow.content.Message;
import org.finos.symphony.toolkit.workflow.content.User;
import org.finos.symphony.toolkit.workflow.content.Word;
import org.finos.symphony.toolkit.workflow.history.History;
import org.finos.symphony.toolkit.workflow.response.MessageResponse;
import org.finos.symphony.toolkit.workflow.response.handlers.ResponseHandlers;
import org.finos.symphony.toolkit.workflow.room.Rooms;
import org.finos.symphony.toolkit.workflow.sources.symphony.history.SymphonyHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.rometools.rome.io.FeedException;

@Controller
public class FeedController {

	public static final Logger LOG = LoggerFactory.getLogger(FeedList.class);

	@Autowired
	SymphonyHistory hist;
	
	@Autowired
	FeedLoader loader;
	
	@Autowired
	FeedListCache rc;
	
	@Autowired
	Notifier n;
	
	@Autowired
	Rooms r;
	
	@Autowired
	ResponseHandlers rh;
	
	@ChatRequest(value="subscriptions", description = "Show RSS Feeds Published In This Room")
	public FeedList getFeedList(Addressable a) {
		Optional<FeedList> fl = hist.getLastFromHistory(FeedList.class, a);
		FeedList ob = fl.orElse(new FeedList());
		return ob;
	}

	@ChatRequest(description = "Subscribe to a feed. ", value="subscribe")
	@ChatButton(value = FeedList.class, showWhen = WorkMode.VIEW, buttonText = "add")
	@ChatResponseBody(workMode = WorkMode.EDIT)
	public SubscribeRequest newSubscribeRequest() {
		return new SubscribeRequest();
	}

	@ChatButton(value = SubscribeRequest.class, buttonText = "add")
	public FeedList subscribe(SubscribeRequest sr, Addressable a, User author, Optional<FeedList> ofl) throws Exception {
		FeedList fl = ofl.orElse(new FeedList());
		adminCheck(author, a, fl);
		try {
			Feed feed = loader.createFeed(sr.url, sr.name);
			if (!fl.feeds.contains(feed)) {
				fl.feeds.add(feed);
			}
			n.sendSuccessNotification(sr, a, author);
			rc.writeFeedList(a, fl);
			return fl;
		} catch (FeedException e) {
			n.sendFailureNotification(sr, a, e, author);
			LOG.error("Couldn't add feed: ", e);
			return null;
		}
	}

	/**
	 * Throws an exception if the author is not a room admin
	 */
	private void adminCheck(User author, Addressable a, FeedList fl) {
		if (fl.adminOnly) {
			if (a instanceof Chat) {
				if (!r.getRoomAdmins((Chat) a).contains(author)) {
					throw new RuntimeException("You need to be admin of this room to modify the feed list");
				}
			}
		}
	}

	@ChatRequest(description = "Stop Feeding (can be resumed later)",  value="pause")
	public FeedList pause(FeedListCache rc, Addressable a, User author) {
		FeedList fl = getFeedList(a);
		adminCheck(author, a, fl);
		fl.paused = true;
		rc.writeFeedList(a, fl);
		return fl;
	}

	@ChatRequest(value="resume", description = "Resume feeds if paused")
	public FeedList resume(FeedListCache rc, Addressable a, User author) {
		FeedList fl = getFeedList(a);
		adminCheck(author, a, fl);
		fl.paused = false;
		rc.writeFeedList(a, fl);
		return fl;
	}
	
	@ChatRequest(description = "Fetch latest news now", value="latest") 
	public void latest(TimedAlerter ta, History hist, Addressable a) {
		FeedList fl = getFeedList(a);
		int count = ta.allItems(a, fl);
		if (count == 0) {
			rh.accept(new MessageResponse(a, Message.of(Word.build("No New News Items"))));
		}
	}
	
	@ChatRequest(value = "filter")
	public Filter createFilterForm() {
		return new Filter();
	}
	
	@ChatButton(buttonText = "Add Filter", value = Filter.class) 
	public FeedList filter(Filter f, Addressable a, User author) {
		FeedList fl = getFeedList(a);
		adminCheck(author, a, fl);
		fl.filters.add(f);
		return fl;
	}
	
	
	
	@ChatRequest(description = "Only room admins can modify the feeds", value="makeAdminOnly") 
	public FeedList makeAdminOnly(Addressable a, User author) {
		FeedList fl = getFeedList(a);
		adminCheck(author, a, fl);
		fl.adminOnly = true;
		return fl;
	}
	
	@ChatRequest(value="notAdminOnly", description = "Any room member can modify the feeds (default)") 
	public FeedList notAdminOnly(Addressable a, User author) {
		FeedList fl = getFeedList(a);
		adminCheck(author, a, fl);
		fl.adminOnly = false;
		return fl;
	}
	
	@ChatRequest(description = "Set the rate of refresh (in minutes) e.g. \"/every 10\"", value="every {mins}") 
	public FeedList every(Addressable a, User author, @ChatVariable(name = "mins") Word mins) {
		FeedList fl = getFeedList(a);
		adminCheck(author, a, fl);
		Integer minInt = Integer.parseInt(mins.getText());
		fl.updateIntervalMinutes = minInt;
		rc.writeFeedList(a, fl);
		return fl;
	}
}
