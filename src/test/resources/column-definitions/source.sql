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
    mvccversion bigint DEFAULT 0 NOT NULL,
    externalreferencecode character varying(75),
    accountentryid bigint NOT NULL,
    companyid bigint,
    userid bigint,
    username character varying(75),
    createdate timestamp without time zone,
    modifieddate timestamp without time zone,
    defaultbillingaddressid bigint,
    defaultcpaymentmethodkey character varying(75),
    defaultshippingaddressid bigint,
    parentaccountentryid bigint,
    description text,
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
    statusbyusername character varying(75),
    statusdate timestamp without time zone
);

--
-- Name: testbatchengineexporttask; Type: TABLE; Schema: public; Owner: scheme-converter
--

CREATE TABLE public.testbatchengineexporttask (
    mvccversion bigint DEFAULT 0 NOT NULL,
    uuid_ character varying(75),
    externalreferencecode character varying(75),
    batchengineexporttaskid bigint NOT NULL,
    companyid bigint,
    userid bigint,
    createdate timestamp without time zone,
    modifieddate timestamp without time zone,
    callbackurl character varying(255),
    classname character varying(255),
    content oid,
    contenttype character varying(75),
    endtime timestamp without time zone,
    errormessage text,
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
    mvccversion bigint DEFAULT 0 NOT NULL,
    ctcollectionid bigint DEFAULT 0 NOT NULL,
    contentid bigint NOT NULL,
    groupid bigint,
    companyid bigint,
    repositoryid bigint,
    path_ character varying(255),
    version character varying(75),
    data_ oid,
    size_ bigint
);

--
-- Name: testquartz_blob_triggers; Type: TABLE; Schema: public; Owner: scheme-converter
--

CREATE TABLE public.testquartz_blob_triggers (
    sched_name character varying(120) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    blob_data bytea
);

--
-- Name: testsegmentsentry; Type: TABLE; Schema: public; Owner: scheme-converter
--

CREATE TABLE public.testsegmentsentry (
    mvccversion bigint DEFAULT 0 NOT NULL,
    ctcollectionid bigint DEFAULT 0 NOT NULL,
    uuid_ character varying(75),
    segmentsentryid bigint NOT NULL,
    groupid bigint,
    companyid bigint,
    userid bigint,
    username character varying(75),
    createdate timestamp without time zone,
    modifieddate timestamp without time zone,
    segmentsentrykey character varying(75),
    name text,
    description text,
    active_ boolean,
    criteria text,
    source character varying(75),
    lastpublishdate timestamp without time zone
);

--
-- Name: batchengineimporttask batchengineimporttask_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.batchengineimporttask
    ADD CONSTRAINT batchengineimporttask_pkey PRIMARY KEY (batchengineimporttaskid);


--
-- Name: batchengineimporttaskerror batchengineimporttaskerror_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.batchengineimporttaskerror
    ADD CONSTRAINT batchengineimporttaskerror_pkey PRIMARY KEY (batchengineimporttaskerrorid);


--
-- Name: batchplannermapping batchplannermapping_pkey; Type: CONSTRAINT; Schema: public; Owner: root
--

ALTER TABLE ONLY public.batchplannermapping
    ADD CONSTRAINT batchplannermapping_pkey PRIMARY KEY (batchplannermappingid);

--
-- Name: ix_ffb3395c; Type: INDEX; Schema: public; Owner: scheme-converter
--

CREATE INDEX ix_ff9d0743 ON public.testsegmentsentry USING btree (groupid, segmentsentrykey, uuid_);

--
-- Name: ix_ffb3395c; Type: INDEX; Schema: public; Owner: scheme-converter
--

CREATE INDEX ix_ffb3395c ON public.testdlcontent USING btree (contentid);

--
-- Name: ix_ffc978a3; Type: INDEX; Schema: public; Owner: scheme-converter
--

CREATE INDEX ix_ffc978a3 ON public.testaccountentry USING btree (accountentryid);

--
-- Name: testdlcontent update_dlcontent_data_; Type: RULE; Schema: public; Owner: scheme-converter
--

CREATE RULE update_dlcontent_data_ AS
    ON UPDATE TO public.testdlcontent
   WHERE ((old.data_ IS DISTINCT FROM new.data_) AND (old.data_ IS NOT NULL)) DO  SELECT
      CASE
          WHEN (EXISTS ( SELECT 1
              FROM pg_largeobject_metadata
            WHERE (pg_largeobject_metadata.oid = old.data_))) THEN lo_unlink(old.data_)
          ELSE NULL::integer
      END AS "case"
   FROM public.testdlcontent
  WHERE (testdlcontent.data_ = old.data_);

--
-- Name: testbatchengineexporttask delete_testbatchengineexporttask_content; Type: RULE; Schema: public; Owner: scheme-converter
--

CREATE RULE delete_testbatchengineexporttask_content AS
    ON DELETE TO public.testbatchengineexporttask DO  SELECT
        CASE
            WHEN (EXISTS ( SELECT 1
               FROM pg_largeobject_metadata
              WHERE (pg_largeobject_metadata.oid = old.content))) THEN lo_unlink(old.content)
            ELSE NULL::integer
END AS "case"
   FROM public.testbatchengineexporttask
  WHERE (testbatchengineexporttask.content = old.content);


--
-- PostgreSQL database dump complete
--

