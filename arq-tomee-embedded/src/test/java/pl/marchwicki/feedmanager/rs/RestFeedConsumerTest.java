package pl.marchwicki.feedmanager.rs;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import pl.marchwicki.feedmanager.FeedsRepository;
import pl.marchwicki.feedmanager.FeedsService;
import pl.marchwicki.feedmanager.InMemoryFeedsRepository;
import pl.marchwicki.feedmanager.model.FeedBuilder;

import com.github.kevinsawicki.http.HttpRequest;

@RunWith(Arquillian.class)
public class RestFeedConsumerTest {

	private final String FEED_NAME = "javalobby";
	
	@Inject
	FeedsRepository repository;
	
	@Deployment
	public static WebArchive createDeployment() throws Exception {
		return ShrinkWrap
				.create(WebArchive.class, "test.war")
				.addClass(RestFeedConsumerEndpoint.class)
				.addClasses(FeedsService.class, FeedBuilder.class)
				.addClass(InMemoryFeedsRepository.class)
				.addAsWebInfResource(EmptyAsset.INSTANCE,
						ArchivePaths.create("beans.xml"));
	}

	@Test
	public void shouldParseXmlFeedTest(@ArquillianResource URL baseURL) throws Exception {
		//given
		URL dir_url = RestFeedConsumerTest.class.getResource("/");
		File dir = new File(dir_url.toURI());
		File xml = new File(dir, "feed.xml");
		
		//when
		int statusCode = HttpRequest.post(baseURL.toURI() + "rs/feed/"+FEED_NAME)
			.send(xml).code();
		
		//then
		assertThat(statusCode, equalTo(201));
		assertThat(repository.getAllFeeds().size(), equalTo(1));
		assertThat(repository.getFeed(FEED_NAME).getItems().size(), equalTo(15));
	}
	
}