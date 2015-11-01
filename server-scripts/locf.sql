
CREATE OR REPLACE FUNCTION locf_t(a character varying(50000), b character varying(50000)) RETURNS character varying(50000) LANGUAGE SQL AS '
SELECT COALESCE(b, a) ';


CREATE AGGREGATE locf(character varying(50000)) ( SFUNC = locf_t, STYPE = character varying(50000));


DROP TYPE IF EXISTS locf_datavalues;


CREATE TYPE locf_datavalues AS (de character varying(11),
 ou character varying(11),
 coc character varying(11),
acoc character varying(11),
 pe character varying(11),
storedby character varying(50000));


CREATE OR REPLACE FUNCTION get_locf_data(startdate character varying(50),enddate character varying(50)) RETURNS
SETOF locf_datavalues AS $$ DECLARE returnrec locf_datavalues; BEGIN
CREATE
TEMPORARY TABLE temp1 (dataelementid integer, sourceid integer, categoryoptioncomboid integer, attributeoptioncomboid integer, startdate date, value character varying (500000), storedby character varying(500000), PRIMARY KEY (sourceid, dataelementid,categoryoptioncomboid,attributeoptioncomboid,startdate) ) ON COMMIT
DROP ; EXECUTE '
INSERT INTO temp1
SELECT *
FROM
  (SELECT d.dataelementid,
          d.sourceid,
          d.categoryoptioncomboid,
          d.attributeoptioncomboid,
          d.startdate,
          locf(d.value::character varying(50000)) OVER (PARTITION BY dataelementid,sourceid,categoryoptioncomboid,attributeoptioncomboid
                                                        ORDER BY startdate) AS value,
          locf(d.storedby::character varying(50000)) OVER (PARTITION BY dataelementid,sourceid,categoryoptioncomboid,attributeoptioncomboid
                                                           ORDER BY startdate) AS startdate
   FROM
     (SELECT c.dataelementid,
             c.sourceid,
             c.categoryoptioncomboid,
             c.attributeoptioncomboid,
             c.startdate,
             dv2.value,
             dv2.storedby
      FROM
        (SELECT DISTINCT dv.dataelementid,
                         dv.sourceid,
                         dv.categoryoptioncomboid,
                         dv.attributeoptioncomboid,
                         b.startdate
         FROM datavalue dv
         INNER JOIN
           (SELECT DISTINCT periodid,
                            startdate
            FROM period
            WHERE startdate >= ''' || $1  || '''::date
              AND startdate <= ''' || $2  || '''::date
              AND periodtypeid = 1) p ON dv.periodid = p.periodid
         AND dataelementid IN
           (SELECT DISTINCT dataelementid
            FROM dataelement
            WHERE name ~(''STOCK[ _]ON[ _]HAND'')
              AND name !~(''MONTH'')) CROSS
         JOIN
           (SELECT generate_series(''' || $1 || '''::date,''' || $2 || '''::date, ''1 DAY'')::date AS startdate) b) AS c
      LEFT OUTER JOIN
        (SELECT dataelementid,
                sourceid,
                categoryoptioncomboid,
                attributeoptioncomboid,
                p.startdate,
                value,
                storedby
         FROM datavalue NATURAL
         JOIN period p) dv2 ON c.dataelementid = dv2.dataelementid
      AND c.sourceid = dv2.sourceid
      AND c.categoryoptioncomboid = dv2.categoryoptioncomboid
      AND c.attributeoptioncomboid = dv2.attributeoptioncomboid
      AND c.startdate = dv2.startdate) d) e
WHERE e.value IS NOT NULL';

 EXECUTE 'WITH datavalues AS
    (SELECT DISTINCT dv.dataelementid,
                     dv.sourceid,
                     dv.categoryoptioncomboid,
                     dv.attributeoptioncomboid,
                     p.startdate
     FROM datavalue dv
     INNER JOIN period p ON dv.periodid = p.periodid
     WHERE dataelementid IN
         (SELECT DISTINCT dataelementid
          FROM dataelement
          WHERE name ~(''STOCK[ _]ON[ _]HAND'')
            AND name !~(''MONTH''))
       AND dv.periodid IN
         (SELECT DISTINCT periodid
          FROM period
          WHERE startdate >=''' || $1 || '''::date
            AND startdate <=''' || $2 || '''::date) )
  DELETE
  FROM temp1 a USING datavalues b WHERE a.dataelementid = b.dataelementid
  AND a.sourceid = b.sourceid
  AND a.categoryoptioncomboid = b.categoryoptioncomboid
  AND a.attributeoptioncomboid = b.attributeoptioncomboid
  AND a.startdate = b.startdate';


  FOR returnrec IN
  SELECT de.uid AS de,
         ou.uid AS ou,
         coc.uid AS coc,
         acoc.uid AS acoc,
         to_char(dv.startdate,'YYYYMMDD')::character varying(11) AS pe,
         dv.value,
         dv.storedby
  FROM temp1 dv
  INNER JOIN dataelement de ON dv.dataelementid = de.dataelementid
  INNER JOIN organisationunit ou ON dv.sourceid = ou.organisationunitid
  INNER JOIN categoryoptioncombo coc ON dv.categoryoptioncomboid = coc.categoryoptioncomboid
  INNER JOIN categoryoptioncombo acoc ON dv.attributeoptioncomboid = acoc.categoryoptioncomboid
  ORDER BY ou,
         de,
         coc,
         acoc,
         pe LOOP RETURN NEXT returnrec;
   END LOOP; 
END;
$$ LANGUAGE plpgsql VOLATILE;