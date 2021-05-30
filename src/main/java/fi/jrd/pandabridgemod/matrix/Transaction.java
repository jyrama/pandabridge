package fi.jrd.pandabridgemod.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Transaction {
    private Event[] events;

    public class Event {
        // base keys
        protected String type;
        protected Content content;

        // room event keys
        protected String event_id;
        protected String sender;
        protected Long origin_server_ts;
        protected String room_id;

        public String getType() {
            return this.type;
        }

        public Object getContent() {
            return content;
        }

        public boolean isMessage() {
            return this.type.equals("m.room.message");
        }

    }

    class Content {
        // join event (m.room.member) keys
        private String membership;
        private String displayname;

        // message event (m.room.message) keys
        private String body;
        private String msgtype;
        private String format;
        private String formatted_body;
    }

    class RoomMessageEvent extends Event {
        public RoomMessageEvent(Event e) {
            this.type = e.type;
            this.content = e.content;
            this.event_id = e.event_id;
            this.sender = e.sender;
            this.origin_server_ts = e.origin_server_ts;
            this.room_id = e.room_id;
        }

        public String sender() {
            return this.sender;
        }

        public Message message() {
            return new Message(this.content.body);
        }

        public String body() {
            return this.content.body;
        }
    }

    public List<RoomMessageEvent> getAllMessages() {
        List<RoomMessageEvent> messages = new ArrayList<RoomMessageEvent>();

        for (Event event : this.events) {
            if (event.isMessage()) {
                messages.add(new RoomMessageEvent(event));
            }
        }

        return messages;
    }

    public List<RoomMessageEvent> getAllTextMessages() {
        Stream<RoomMessageEvent> filtered = this.getAllMessages().stream().filter(m -> m.content.msgtype.equals("m.text"));
        return filtered.collect(Collectors.toList());
    }

    public Event[] getAllEvents() {
        return events;
    }
}
