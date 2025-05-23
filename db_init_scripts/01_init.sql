CREATE TABLE Users
(
    id            SERIAL PRIMARY KEY CHECK ( id > 0 ),
    name          VARCHAR(64)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL
);

CREATE TYPE EventType AS ENUM ('E_SPORTS', 'FOOTBALL', 'BASKETBALL', 'OPERA', 'EXPOSITION');

CREATE TABLE Event
(
    id          SERIAL PRIMARY KEY CHECK ( id > 0 ),
    name        TEXT          NOT NULL,
    description VARCHAR(1573) NOT NULL,
    event_type  EventType
);

CREATE TYPE TicketType AS ENUM ('VIP', 'USUAL', 'BUDGETARY', 'CHEAP');

CREATE TABLE Ticket
(
    id            SERIAL PRIMARY KEY CHECK ( id > 0 ),
    name          TEXT       NOT NULL,
    x             FLOAT      NOT NULL,
    y             FLOAT      NOT NULL CHECK ( y <= 654.00 ),
    creation_date TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ticket_type   TicketType NOT NULL,
    price         FLOAT      NOT NULL CHECK ( price > 0.00 ),
    creator_id    INT REFERENCES Users (id),
    event_id      int REFERENCES Event (id)
);

