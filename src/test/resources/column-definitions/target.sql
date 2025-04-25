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
-- Name: testaccountentry; Type: TABLE; Schema: public; Owner: scheme-converter
--

CREATE TABLE public.testaccountentry (
    externalreferencecode character varying(75),
    accountentryid bigint,
    companyid bigint,
    userid bigint,
    username character varying(75),
    createdate timestamp without time zone,
    modifieddate timestamp without time zone,
    defaultbillingaddressid bigint,
    defaultcpaymentmethodkey character varying(75),
    defaultshippingaddressid bigint,
    parentaccountentryid bigint,
    description unknown,
    domains text,
    emailaddress character varying(254),
    logoid bigint,
    name character varying(250),
    taxexemptioncode character varying(75),
    taxidnumber character varying(75),
    type_ character varying(75),
    status integer,
    uuid_ character varying(75),
    restrictmembership boolean,
    statusbyuserid bigint,
    mvccversion bigint DEFAULT 0 NOT NULL,
    statusbyusername character varying(75),
    statusdate timestamp without time zone
);

--
-- Name: testbatchengineexporttask; Type: TABLE; Schema: public; Owner: scheme-converter
--

CREATE TABLE public.testbatchengineexporttask (
    mvccversion bigint,
    uuid_ character varying(75),
    externalreferencecode character varying(75),
    batchengineexporttaskid bigint,
    companyid bigint,
    userid bigint,
    createdate timestamp without time zone,
    modifieddate timestamp without time zone,
    callbackurl character varying(255),
    classname character varying(255),
    content unknown,
    contenttype character varying(75),
    endtime timestamp without time zone,
    errormessage unknown,
    fieldnames text,
    executestatus character varying(75),
    parameters text,
    processeditemscount integer,
    starttime timestamp without time zone,
    taskitemdelegatename character varying(75),
    totalitemscount integer
);

--
-- Name: testcommercepricemodifierrel; Type: TABLE; Schema: public; Owner: scheme-converter
--

--
-- Name: testdlcontent; Type: TABLE; Schema: public; Owner: scheme-converter
--

CREATE TABLE public.testdlcontent (
    mvccversion bigint,
    ctcollectionid bigint,
    contentid bigint,
    groupid bigint,
    companyid bigint,
    repositoryid bigint,
    path_ character varying(255),
    version character varying(75),
    data_ unknown,
    size_ bigint
);

--
-- Name: testquartz_blob_triggers; Type: TABLE; Schema: public; Owner: scheme-converter
--

CREATE TABLE public.testquartz_blob_triggers (
    sched_name character varying(120),
    "TRIGGER_NAME" character varying(200),
    trigger_group character varying(200),
    blob_data bytea
);

--
-- Name: testsegmentsentry; Type: TABLE; Schema: public; Owner: scheme-converter
--

CREATE TABLE public.testsegmentsentry (
    ctcollectionid bigint,
    uuid_ character varying(75),
    groupid bigint,
    companyid bigint,
    segmentsentryid bigint,
    userid bigint,
    username character varying(75),
    createdate timestamp without time zone,
    modifieddate timestamp without time zone,
    segmentsentrykey character varying(75),
    name text,
    description text,
    active_ smallint,
    criteria text,
    source character varying(75),
    mvccversion bigint,
    lastpublishdate timestamp without time zone
);


--
-- PostgreSQL database dump complete
--


