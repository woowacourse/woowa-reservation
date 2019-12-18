package com.h3.reservation.slack.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.h3.reservation.common.MeetingRoom;
import com.h3.reservation.common.ReservationDetails;
import com.h3.reservation.slack.EventType;
import com.h3.reservation.slack.InitMenuType;
import com.h3.reservation.slack.dto.request.BlockActionRequest;
import com.h3.reservation.slack.dto.request.viewsubmission.CancelRequest;
import com.h3.reservation.slack.dto.request.EventCallbackRequest;
import com.h3.reservation.slack.dto.request.VerificationRequest;
import com.h3.reservation.slack.dto.request.viewsubmission.ChangeRequest;
import com.h3.reservation.slack.dto.request.viewsubmission.ReserveRequest;
import com.h3.reservation.slack.dto.request.viewsubmission.RetrieveRequest;
import com.h3.reservation.slack.dto.response.ModalResponse;
import com.h3.reservation.slack.dto.response.ModalUpdateResponse;
import com.h3.reservation.slack.dto.response.factory.InitHomeTabResponseFactory;
import com.h3.reservation.slack.dto.response.factory.InitResponseFactory;
import com.h3.reservation.slack.dto.response.factory.modalpush.CancelPushResponseFactory;
import com.h3.reservation.slack.dto.response.factory.modalpush.ChangePushResponseFactory;
import com.h3.reservation.slack.dto.response.factory.modalupdate.CancelFinishedResponseFactory;
import com.h3.reservation.slack.dto.response.factory.modalupdate.ChangeFinishedResponseFactory;
import com.h3.reservation.slack.dto.response.factory.modalupdate.ChangeModalUpdateResponseFactory;
import com.h3.reservation.slack.dto.response.factory.modalupdate.ReserveModalUpdateResponseFactory;
import com.h3.reservation.slack.dto.response.factory.modalupdate.RetrieveModalUpdateResponseFactory;
import com.h3.reservation.slackcalendar.domain.DateTime;
import com.h3.reservation.slackcalendar.domain.Reservation;
import com.h3.reservation.slackcalendar.domain.Reservations;
import com.h3.reservation.slackcalendar.service.SlackCalendarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalTime;

/**
 * @author heebg
 * @version 1.0
 * @date 2019-12-02
 */
@Service
public class SlackService {
    private static final Logger logger = LoggerFactory.getLogger(SlackService.class);

    private static final String BASE_URL = "https://slack.com/api";
    private static final String TOKEN = "Bearer " + System.getenv("BOT_TOKEN");

    private final SlackCalendarService slackCalendarService;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public SlackService(SlackCalendarService slackCalendarService, ObjectMapper objectMapper) {
        this.slackCalendarService = slackCalendarService;
        this.objectMapper = objectMapper;
        this.webClient = initWebClient();
    }

    public String verify(VerificationRequest dto) {
        return dto.getChallenge();
    }

    public void showMenu(EventCallbackRequest dto) {
        switch (EventType.of(dto.getType())) {
            case APP_MENTION:
                send("/chat.postMessage", InitResponseFactory.of(dto.getChannel()));
                break;
            case APP_HOME_OPENED:
                send("/views.publish", InitHomeTabResponseFactory.of(dto.getUserId()));
        }
    }

    public void showModal(BlockActionRequest dto) {
        if (dto.getBlockId().equals("initial_block")) {
            send("/views.open", InitMenuType.of(dto.getActionId()).apply(dto.getTriggerId()));
            return;
        }
        if (dto.getActionId().startsWith("request_change")) {
            send("/views.push", pushChangeModal(dto));
            return;
        }
        if (dto.getActionId().startsWith("request_cancel")) {
            send("/views.push", pushCancelModal(dto));
        }
    }

    private ModalResponse pushChangeModal(BlockActionRequest dto) {
        String reservationId = parseReservationId(dto.getActionId());
        Reservation reservation = slackCalendarService.retrieveById(reservationId);
        return ChangePushResponseFactory.of(dto.getTriggerId(), reservation);
    }

    private String parseReservationId(String id) {
        String ID_REG = "_";
        int ID_INDEX = 2;
        return id.split(ID_REG)[ID_INDEX];
    }

    private ModalResponse pushCancelModal(BlockActionRequest dto) {
        String reservationId = parseReservationId(dto.getActionId());
        Reservation reservation = slackCalendarService.retrieveById(reservationId);
        return CancelPushResponseFactory.of(dto.getTriggerId(), reservation);
    }

    public ModalUpdateResponse updateRetrieveModal(RetrieveRequest request) {
        DateTime retrieveRangeDateTime = DateTime.of(request.getDate()
            , generateLocalTime(request.getStartHour(), request.getStartMinute())
            , generateLocalTime(request.getEndHour(), request.getEndMinute()));
        Reservations reservations = slackCalendarService.retrieve(retrieveRangeDateTime);
        return RetrieveModalUpdateResponseFactory.of(retrieveRangeDateTime, reservations);
    }

    private LocalTime generateLocalTime(String hour, String minute) {
        return LocalTime.of(Integer.parseInt(hour), Integer.parseInt(minute));
    }

    public ModalUpdateResponse updateReservationModal(ReserveRequest request) throws IOException {
        ReservationDetails details = ReservationDetails.of(
            MeetingRoom.findByName(request.getMeetingRoom()), request.getName(), request.getDescription()
        );
        DateTime dateTime = DateTime.of(request.getDate()
            , generateLocalTime(request.getStartHour(), request.getStartMinute())
            , generateLocalTime(request.getEndHour(), request.getEndMinute()));

        return ReserveModalUpdateResponseFactory.of(slackCalendarService.reserve(details, dateTime));
    }

    public ModalUpdateResponse updateChangeModal(ChangeRequest request) {
        Reservations reservations = slackCalendarService.retrieve(request.getDate(), request.getName());
        return ChangeModalUpdateResponseFactory.of(reservations);
    }

    public ModalUpdateResponse updateChangeRequestModal(ReserveRequest request) {
        String reservationId = request.getPrivateMetadata();
        ReservationDetails details = ReservationDetails.of(
            MeetingRoom.findByName(request.getMeetingRoom()), request.getName(), request.getDescription()
        );
        DateTime dateTime = DateTime.of(request.getDate()
            , generateLocalTime(request.getStartHour(), request.getStartMinute())
            , generateLocalTime(request.getEndHour(), request.getEndMinute()));
        Reservation reservation = slackCalendarService.change(Reservation.of(reservationId, details, dateTime));
        return ChangeFinishedResponseFactory.of(reservation);
    }

    public ModalUpdateResponse updateCancelRequestModal(CancelRequest request) {
        String reservationId = request.getPrivateMetadata();
        Reservation reservation = slackCalendarService.retrieveById(reservationId);
        slackCalendarService.cancel(reservation);
        return CancelFinishedResponseFactory.of(reservation);
    }

    private WebClient initWebClient() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(config ->
                config.customCodecs().encoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON))
            ).build();
        return WebClient.builder()
            .exchangeStrategies(strategies)
            .baseUrl(BASE_URL)
            .defaultHeader(HttpHeaders.AUTHORIZATION, TOKEN)
            .build();
    }

    private void send(String url, Object dto) {
        String response = webClient.post()
            .uri(url)
            .body(BodyInserters.fromValue(dto))
            .exchange().block().bodyToMono(String.class)
            .block();
        logger.debug("WebClient Response: {}", response);
    }
}
