-- apply changes
create table rc_graveyards_items (
  id                            integer auto_increment not null,
  storage_id                    integer not null,
  position                      integer not null,
  lootable                      tinyint(1) default 0 not null,
  player_id                     varchar(40),
  world                         varchar(255),
  constraint pk_rc_graveyards_items primary key (id)
);

