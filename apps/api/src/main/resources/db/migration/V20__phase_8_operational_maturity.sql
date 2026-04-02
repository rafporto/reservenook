alter table companies
    add column if not exists legal_hold_until timestamp with time zone;

create table if not exists abuse_prevention_policies (
    id bigint primary key,
    login_pair_limit integer not null,
    login_client_limit integer not null,
    login_email_limit integer not null,
    public_write_pair_limit integer not null,
    public_write_client_limit integer not null,
    public_write_email_limit integer not null,
    public_read_client_limit integer not null,
    updated_at timestamp with time zone not null default now()
);

merge into abuse_prevention_policies (
    id,
    login_pair_limit,
    login_client_limit,
    login_email_limit,
    public_write_pair_limit,
    public_write_client_limit,
    public_write_email_limit,
    public_read_client_limit
)
key (id)
values (
    1,
    5,
    10,
    10,
    5,
    10,
    10,
    20
);
