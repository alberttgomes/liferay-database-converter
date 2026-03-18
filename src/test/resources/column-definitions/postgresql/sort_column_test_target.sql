--
-- PostgreSQL database dump
--

-- Dumped from database version 14.13 (Debian 14.13-1.pgdg120+1)
-- Dumped by pg_dump version 14.13 (Debian 14.13-1.pgdg120+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: testsortcolumn; Type: TABLE; Schema: public; Owner: scheme-converter
--

CREATE TABLE public.testsortcolumn (
    ctcollectionid bigint,
    description varchar,
    uuid_ character varying(75),
    groupid bigint,
    segmentsentryid bigint,
    userid bigint,
    username varchar,
    createdate timestamp without time zone,
    modifieddate timestamp without time zone,
    segmentsentrykey character varying(75),
    name text,
    active_ boolean,
    source character varying(75),
    mvccversion number,
    lastpublishdate timestamp without time zone,
    companyid bigint
);


--
-- PostgreSQL database dump complete
--

