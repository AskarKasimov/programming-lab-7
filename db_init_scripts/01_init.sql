CREATE TABLE Users
(
    id            SERIAL PRIMARY KEY,
    name          VARCHAR(64) NOT NULL,
    password_hash CHAR(96)    NOT NULL
);

CREATE TYPE EventType AS ENUM ('E_SPORTS', 'FOOTBALL', 'BASKETBALL', 'OPERA', 'EXPOSITION');

CREATE TABLE Event
(
    id          SERIAL PRIMARY KEY,
    name        TEXT          NOT NULL,
    description VARCHAR(1573) NOT NULL,
    eventType   EventType
);

CREATE TYPE TicketType AS ENUM ('VIP', 'USUAL', 'BUDGETARY', 'CHEAP');

CREATE TABLE Ticket
(
    id           SERIAL PRIMARY KEY,
    creator_id   INT REFERENCES Users (id),
    name         TEXT       NOT NULL,
    x            FLOAT      NOT NULL,
    y            FLOAT      NOT NULL CHECK ( y <= 654.00 ),
    creationDate TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    price        FLOAT      NOT NULL CHECK ( price > 0.00 ),
    ticketType   TicketType NOT NULL,
    event_id     INT REFERENCES Event (id)
);

