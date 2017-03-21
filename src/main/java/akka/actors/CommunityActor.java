package akka.actors;

import akka.actor.UntypedActor;
import akka.messages.CommunityMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;
import services.solr.TestActors;

import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dorinela on 3/17/2017.
 */
public class CommunityActor extends UntypedActor{

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunityActor.class);

    private static final String START_PROCESSING = "START_PROCESSING";

    @Override
    public void preStart() {
        Long delay = computeDelayOfJob();

        getContext().system().scheduler().scheduleOnce(
                Duration.create(delay, TimeUnit.MILLISECONDS),
                getSelf(), START_PROCESSING, getContext().dispatcher(), null);
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof String && message.equals(START_PROCESSING)) {
            LOGGER.info("Received " + START_PROCESSING + " message.");
            /**
             * create CommunityMessage to send to SolrDataProcessingActor
             */
            CommunityMessage communityMessage = new CommunityMessage("Games");
            /**
             * send message to SolrDataProcessingActor
             */
            LOGGER.info("Send CommunityMessage to SolrDataProcessingActor.");
            TestActors.akkaActorSystem.solrDataProcessingActor.tell(communityMessage, self());

            /**
             * trigger job at 12 PM every day
             */
//            getContext().system().scheduler().scheduleOnce(
//                    Duration.create(computeDelayOfJob(), TimeUnit.MILLISECONDS),
//                    getSelf(), START_PROCESSING, getContext().dispatcher(), null);

        } else {
            LOGGER.warn("Unhandled message.");
            unhandled(message);
        }

    }

    /**
     * compute the time until the job starts
     *
     * In this case the job will start every day at 12:00 PM
     *
     * @return
     */
    private Long computeDelayOfJob() {
        GregorianCalendar gCalendar = new GregorianCalendar();
        gCalendar.set(GregorianCalendar.HOUR_OF_DAY, 24);
        gCalendar.set(GregorianCalendar.MINUTE, 0);
        gCalendar.set(GregorianCalendar.SECOND, 0);
        Long delay = gCalendar.getTimeInMillis() - System.currentTimeMillis();

        if (delay < 0) {
            gCalendar.add(GregorianCalendar.DAY_OF_MONTH, 1);
            delay = gCalendar.getTimeInMillis() - System.currentTimeMillis();
        }

        return  delay;
    }
}
