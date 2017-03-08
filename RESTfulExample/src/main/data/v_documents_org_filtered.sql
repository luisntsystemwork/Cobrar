
CREATE OR REPLACE FUNCTION v_documents_org_filtered(
    bpartner integer,
    summaryonly boolean,
    condition character)
  RETURNS SETOF v_documents_org_type_condition AS
$BODY$
declare
    consulta varchar;
    orderby1 varchar;
    orderby2 varchar;
    orderby3 varchar;
    leftjoin1 varchar;
    leftjoin2 varchar;
    whereclause1 varchar;
    whereclause2 varchar;
    whereclause3 varchar;
    advancedcondition varchar;
    adocument v_documents_org_type_condition;
   
BEGIN
    -- recuperar informacion minima indispensable si summaryonly es true.  en caso de ser false, debe joinearse/ordenarse, etc.
    if summaryonly = false then

        orderby1 = ' ORDER BY ''C_Invoice''::text, i.c_invoice_id, i.ad_client_id, i.ad_org_id, i.isactive, i.created, i.createdby, i.updated, i.updatedby, i.c_bpartner_id, i.c_doctype_id, dt.signo_issotrx, dt.name, dt.printname, i.documentno, i.issotrx, i.docstatus,
                 CASE
                     WHEN i.c_invoicepayschedule_id IS NOT NULL THEN ips.duedate
                     ELSE i.dateinvoiced
                 END, i.dateacct, i.c_currency_id, i.c_conversiontype_id, i.grandtotal, i.c_invoicepayschedule_id, ips.duedate, i.dateinvoiced, bp.socreditstatus ';

        orderby2 = ' ORDER BY ''C_Payment''::text, p.c_payment_id, p.ad_client_id, COALESCE(il.ad_org_id, p.ad_org_id), p.isactive, p.created, p.createdby, p.updated, p.updatedby, p.c_bpartner_id, p.c_doctype_id, dt.signo_issotrx, dt.name, dt.printname, p.documentno, p.issotrx, p.docstatus, p.datetrx, p.dateacct, p.c_currency_id, p.c_conversiontype_id, p.payamt, NULL::integer, p.duedate, bp.socreditstatus ';

        orderby3 = ' ORDER BY ''C_CashLine''::text, cl.c_cashline_id, cl.ad_client_id, COALESCE(il.ad_org_id, cl.ad_org_id), cl.isactive, cl.created, cl.createdby, cl.updated, cl.updatedby,
                CASE
                    WHEN cl.c_bpartner_id IS NOT NULL THEN cl.c_bpartner_id
                    ELSE il.c_bpartner_id
                END, dt.c_doctype_id,
                CASE
                    WHEN cl.amount < 0.0 THEN 1
                    ELSE (-1)
                END, dt.name, dt.printname, ''@line@''::text || cl.line::character varying::text,
                CASE
                    WHEN cl.amount < 0.0 THEN ''N''::bpchar
                    ELSE ''Y''::bpchar
                END, cl.docstatus, c.statementdate, c.dateacct, cl.c_currency_id, NULL::integer, abs(cl.amount), NULL::timestamp without time zone, COALESCE(bp.socreditstatus, bp2.socreditstatus) ';
   
    else
        orderby1 = '';
        orderby2 = '';
        orderby3 = '';

    end if;

    --Si no se deben mostrar todos, entonces agregar la condicion por la forma de pago
    if condition <> 'A' then
	--Si se debe mostrar sólo efectivo, entonces no se debe mostrar los anticipos, si o si debe tener una factura asociada
	advancedcondition = 'il.paymentrule is null OR ';
	if condition = 'B' then
		advancedcondition = '';
	end if;
	whereclause1 = ' (i.paymentrule = ''' || condition || ''') ';
	whereclause2 = ' (' || advancedcondition || ' il.paymentrule = ''' || condition || ''') ';
	whereclause3 = ' (' || advancedcondition || ' il.paymentrule = ''' || condition || ''') ';
    else
	whereclause1 = ' (1 = 1) ';
	whereclause2 = ' (1 = 1) ';
	whereclause3 = ' (1 = 1) ';
    end if;

    consulta = '

        (        ( SELECT DISTINCT ''C_Invoice''::text AS documenttable, i.c_invoice_id AS document_id, i.ad_client_id, i.ad_org_id, i.isactive, i.created, i.createdby, i.updated, i.updatedby, i.c_bpartner_id, i.c_doctype_id, dt.signo_issotrx, dt.name AS doctypename, dt.printname AS doctypeprintname, i.documentno, i.issotrx, i.docstatus,
                        CASE
                            WHEN i.c_invoicepayschedule_id IS NOT NULL THEN ips.duedate
                            ELSE i.dateinvoiced
                        END AS datetrx, i.dateacct, i.c_currency_id, i.c_conversiontype_id, i.grandtotal AS amount, i.c_invoicepayschedule_id, ips.duedate, i.dateinvoiced AS truedatetrx, bp.socreditstatus, i.c_order_id
                   FROM c_invoice_v i
              JOIN c_doctype dt ON i.c_doctypetarget_id = dt.c_doctype_id
         JOIN c_bpartner bp ON bp.c_bpartner_id = i.c_bpartner_id and (' || $1 || ' = -1  or bp.c_bpartner_id = ' || $1 || ')
    LEFT JOIN c_invoicepayschedule ips ON i.c_invoicepayschedule_id = ips.c_invoicepayschedule_id
    WHERE 

' || whereclause1 || '
' || orderby1 || '

    )
        UNION ALL
                ( SELECT DISTINCT ''C_Payment''::text AS documenttable, p.c_payment_id AS document_id, p.ad_client_id, COALESCE(il.ad_org_id, p.ad_org_id) AS ad_org_id, p.isactive, p.created, p.createdby, p.updated, p.updatedby, p.c_bpartner_id, p.c_doctype_id, dt.signo_issotrx, dt.name AS doctypename, dt.printname AS doctypeprintname, p.documentno, p.issotrx, p.docstatus, p.datetrx, p.dateacct, p.c_currency_id, p.c_conversiontype_id, p.payamt AS amount, NULL::integer AS c_invoicepayschedule_id, p.duedate, p.datetrx AS truedatetrx, bp.socreditstatus, 0 as c_order_id
                   FROM c_payment p
              JOIN c_doctype dt ON p.c_doctype_id = dt.c_doctype_id
         JOIN c_bpartner bp ON p.c_bpartner_id = bp.c_bpartner_id AND (' || $1 || ' = -1 or p.c_bpartner_id = ' || $1 || ')
	LEFT JOIN c_allocationline al ON al.c_payment_id = p.c_payment_id
	LEFT JOIN c_invoice il ON il.c_invoice_id = al.c_invoice_id
  WHERE 
CASE
    WHEN il.ad_org_id IS NOT NULL AND il.ad_org_id <> p.ad_org_id THEN p.docstatus = ANY (ARRAY[''CO''::bpchar, ''CL''::bpchar])
    ELSE 1 = 1
END 

AND ' || whereclause2 || '

' || orderby2 || '


))

UNION ALL

        ( SELECT DISTINCT ''C_CashLine''::text AS documenttable, cl.c_cashline_id AS document_id, cl.ad_client_id, COALESCE(il.ad_org_id, cl.ad_org_id) AS ad_org_id, cl.isactive, cl.created, cl.createdby, cl.updated, cl.updatedby,
                CASE
                    WHEN cl.c_bpartner_id IS NOT NULL THEN cl.c_bpartner_id
                    ELSE il.c_bpartner_id
                END AS c_bpartner_id, dt.c_doctype_id,
                CASE
                    WHEN cl.amount < 0.0 THEN 1
                    ELSE (-1)
                END AS signo_issotrx, dt.name AS doctypename, dt.printname AS doctypeprintname, ''@line@''::text || cl.line::character varying::text AS documentno,
                CASE
                    WHEN cl.amount < 0.0 THEN ''N''::bpchar
                    ELSE ''Y''::bpchar
                END AS issotrx, cl.docstatus, c.statementdate AS datetrx, c.dateacct, cl.c_currency_id, NULL::integer AS c_conversiontype_id, abs(cl.amount) AS amount, NULL::integer AS c_invoicepayschedule_id, NULL::timestamp without time zone AS duedate, c.statementdate AS truedatetrx, COALESCE(bp.socreditstatus, bp2.socreditstatus) AS socreditstatus, 0 as c_order_id
           FROM c_cashline cl
      JOIN c_cash c ON cl.c_cash_id = c.c_cash_id
   LEFT JOIN c_bpartner bp ON cl.c_bpartner_id = bp.c_bpartner_id AND (' || $1 || ' = -1 or cl.c_bpartner_id = ' || $1 || ')
   JOIN ( SELECT d.ad_client_id, d.c_doctype_id, d.name, d.printname
         FROM c_doctype d
        WHERE d.doctypekey::text = ''CMC''::text) dt ON cl.ad_client_id = dt.ad_client_id
   LEFT JOIN c_allocationline al ON al.c_cashline_id = cl.c_cashline_id
   LEFT JOIN c_invoice il ON il.c_invoice_id = al.c_invoice_id AND (' || $1 || ' = -1 or il.c_bpartner_id = ' || $1 || ')
   LEFT JOIN c_bpartner bp2 ON il.c_bpartner_id = bp2.c_bpartner_id
  WHERE (CASE WHEN cl.c_bpartner_id IS NOT NULL THEN (' || $1 || ' = -1 or cl.c_bpartner_id = ' || $1 || ')
        WHEN il.c_bpartner_id IS NOT NULL THEN (' || $1 || ' = -1 or il.c_bpartner_id = ' || $1 || ')
        ELSE 1 = 2 END)
    AND (CASE WHEN il.ad_org_id IS NOT NULL AND il.ad_org_id <> cl.ad_org_id
        THEN cl.docstatus = ANY (ARRAY[''CO''::bpchar, ''CL''::bpchar])
        ELSE 1 = 1 END)

    AND ' || whereclause3 || '

' || orderby3 || '

); ';

-- raise notice '%', consulta;
FOR adocument IN EXECUTE consulta LOOP
	return next adocument;
END LOOP;

END
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION v_documents_org_filtered(integer, boolean, character)
  OWNER TO libertya;

  
--DROP de función v_documents_org_filtered(integer, boolean, character) y tipo v_documents_org_type_condition 
DROP FUNCTION v_documents_org_filtered(integer, boolean, character);
--DROP TYPE v_documents_org_type_condition;
-- Tipo v_documents_org_type_condition
--CREATE TYPE v_documents_org_type_condition AS (documenttable text, document_id int, ad_client_id int, ad_org_id int, 
--					isactive char(1), created timestamp, createdby integer, updated timestamp, 
--					updatedby int, c_bpartner_id int, c_doctype_id integer, signo_issotrx int, 
--					doctypename varchar(60), doctypeprintname varchar(60), documentno varchar(60), 
--					issotrx bpchar, docstatus character(2), datetrx timestamp, dateacct timestamp, 
--					c_currency_id int, c_conversiontype_id int, amount numeric, 
--					c_invoicepayschedule_id integer, duedate timestamp, truedatetrx timestamp, 
--					socreditstatus char(1), c_order_id integer, c_allocationhdr_id integer);

--Función v_documents_org_filtered(integer, boolean, character, timestamp without time zone)
CREATE OR REPLACE FUNCTION v_documents_org_filtered(
    bpartner integer,
    summaryonly boolean,
    condition character,
    dateto timestamp without time zone)
  RETURNS SETOF v_documents_org_type_condition AS
$BODY$
declare
    consulta varchar;
    orderby1 varchar;
    orderby2 varchar;
    orderby3 varchar;
    leftjoin1 varchar;
    leftjoin2 varchar;
    advancedcondition varchar;
    whereclauseConditionDebit varchar;
    whereclauseConditionCredit varchar;
    whereclauseDateTo varchar;
    selectallocationNull varchar;
    selectallocationPayment varchar;
    selectallocationCashline varchar;
    selectallocationCredit varchar;
    selectAllocationReferencePayment varchar;
    selectAllocationReferenceCashline varchar;
    selectAllocationReferenceCredit varchar;
    adocument v_documents_org_type_condition;
   
BEGIN
    whereclauseDateTo = ' ( 1 = 1 ) ';
    -- Armar la condición para fecha de corte
    if dateTo is not null then 
	whereclauseDateTo = ' dateacct::date <= ''' || dateTo || '''::date ';
    end if;
    
    --Si no se deben mostrar todos, entonces agregar la condicion por la forma de pago
    if condition <> 'A' then
	--Si se debe mostrar sólo efectivo, entonces no se debe mostrar los anticipos, si o si debe tener una factura asociada
	advancedcondition = 'il.paymentrule is null OR ';
	if condition = 'B' then
		advancedcondition = '';
	end if;
	whereclauseConditionDebit = ' (i.paymentrule = ''' || condition || ''') ';
	whereclauseConditionCredit = ' (' || advancedcondition || ' il.paymentrule = ''' || condition || ''') ';
    else
	whereclauseConditionDebit = ' (1 = 1) ';
	whereclauseConditionCredit = ' (1 = 1) ';
    end if;    

    -- recuperar informacion minima indispensable si summaryonly es true.  en caso de ser false, debe joinearse/ordenarse, etc.
    if summaryonly = false then

        orderby1 = ' ORDER BY ''C_Invoice''::text, i.c_invoice_id, i.ad_client_id, i.ad_org_id, i.isactive, i.created, i.createdby, i.updated, i.updatedby, i.c_bpartner_id, i.c_doctype_id, dt.signo_issotrx, dt.name, dt.printname, i.documentno, i.issotrx, i.docstatus,
                 CASE
                     WHEN i.c_invoicepayschedule_id IS NOT NULL THEN ips.duedate
                     ELSE i.dateinvoiced
                 END, i.dateacct, i.c_currency_id, i.c_conversiontype_id, i.grandtotal, i.c_invoicepayschedule_id, ips.duedate, i.dateinvoiced, bp.socreditstatus ';

        orderby2 = ' ORDER BY ''C_Payment''::text, p.c_payment_id, p.ad_client_id, COALESCE(il.ad_org_id, p.ad_org_id), p.isactive, p.created, p.createdby, p.updated, p.updatedby, p.c_bpartner_id, p.c_doctype_id, dt.signo_issotrx, dt.name, dt.printname, p.documentno, p.issotrx, p.docstatus, p.datetrx, p.dateacct, p.c_currency_id, p.c_conversiontype_id, p.payamt, NULL::integer, p.duedate, bp.socreditstatus ';

        orderby3 = ' ORDER BY ''C_CashLine''::text, cl.c_cashline_id, cl.ad_client_id, COALESCE(il.ad_org_id, cl.ad_org_id), cl.isactive, cl.created, cl.createdby, cl.updated, cl.updatedby,
                CASE
                    WHEN cl.c_bpartner_id IS NOT NULL THEN cl.c_bpartner_id
                    ELSE il.c_bpartner_id
                END, dt.c_doctype_id,
                CASE
                    WHEN cl.amount < 0.0 THEN 1
                    ELSE (-1)
                END, dt.name, dt.printname, ''@line@''::text || cl.line::character varying::text,
                CASE
                    WHEN cl.amount < 0.0 THEN ''N''::bpchar
                    ELSE ''Y''::bpchar
                END, cl.docstatus, c.statementdate, c.dateacct, cl.c_currency_id, NULL::integer, abs(cl.amount), NULL::timestamp without time zone, COALESCE(bp.socreditstatus, bp2.socreditstatus) ';
	
	selectallocationNull = ' NULL::integer ';
	selectallocationPayment = selectallocationNull;
	selectallocationCashline = selectallocationNull;
	selectallocationCredit = selectallocationNull;
	
    else
        orderby1 = '';
        orderby2 = '';
        orderby3 = '';

	selectAllocationReferencePayment = ' al.c_payment_id = p.c_payment_id ';
	selectAllocationReferenceCashline = ' al.c_cashline_id = cl.c_cashline_id ';
	selectAllocationReferenceCredit = ' al.c_invoice_credit_id = i.c_invoice_id ';

	selectallocationPayment = ' (SELECT ah.c_allocationhdr_id FROM c_allocationline al INNER JOIN c_allocationhdr ah on ah.c_allocationhdr_id = al.c_allocationhdr_id WHERE allocationtype <> ''MAN'' AND ah.dateacct::date = p.dateacct::date AND ' || selectAllocationReferencePayment || ' AND ' || whereclauseDateTo || ' ORDER BY ah.created LIMIT 1) as c_allocationhdr_id ';
	selectallocationCashline = ' (SELECT ah.c_allocationhdr_id FROM c_allocationline al INNER JOIN c_allocationhdr ah on ah.c_allocationhdr_id = al.c_allocationhdr_id WHERE allocationtype <> ''MAN'' AND ah.dateacct::date = c.dateacct::date AND ' || selectAllocationReferenceCashline || ' AND ' || whereclauseDateTo || ' ORDER BY ah.created LIMIT 1) as c_allocationhdr_id ';
	selectallocationCredit = ' (SELECT ah.c_allocationhdr_id FROM c_allocationline al INNER JOIN c_allocationhdr ah on ah.c_allocationhdr_id = al.c_allocationhdr_id WHERE allocationtype <> ''MAN'' AND ah.dateacct::date = i.dateacct::date AND ' || selectAllocationReferenceCredit || ' AND ' || whereclauseDateTo || ' ORDER BY ah.created LIMIT 1) as c_allocationhdr_id ';
	
    end if;    

    consulta = ' SELECT * FROM 

        (        ( SELECT DISTINCT ''C_Invoice''::text AS documenttable, i.c_invoice_id AS document_id, i.ad_client_id, i.ad_org_id, i.isactive, i.created, i.createdby, i.updated, i.updatedby, i.c_bpartner_id, i.c_doctype_id, dt.signo_issotrx, dt.name AS doctypename, dt.printname AS doctypeprintname, i.documentno, i.issotrx, i.docstatus,
                        CASE
                            WHEN i.c_invoicepayschedule_id IS NOT NULL THEN ips.duedate
                            ELSE i.dateinvoiced
                        END AS datetrx, i.dateacct, i.c_currency_id, i.c_conversiontype_id, i.grandtotal AS amount, i.c_invoicepayschedule_id, ips.duedate, i.dateinvoiced AS truedatetrx, bp.socreditstatus, i.c_order_id, '
				|| selectallocationCredit || 
               ' FROM c_invoice_v i
              JOIN c_doctype dt ON i.c_doctypetarget_id = dt.c_doctype_id
         JOIN c_bpartner bp ON bp.c_bpartner_id = i.c_bpartner_id and (' || $1 || ' = -1  or bp.c_bpartner_id = ' || $1 || ')
    LEFT JOIN c_invoicepayschedule ips ON i.c_invoicepayschedule_id = ips.c_invoicepayschedule_id
    WHERE 

' || whereclauseConditionDebit || '
' || orderby1 || '

    )
        UNION ALL
                ( SELECT DISTINCT ''C_Payment''::text AS documenttable, p.c_payment_id AS document_id, p.ad_client_id, COALESCE(il.ad_org_id, p.ad_org_id) AS ad_org_id, p.isactive, p.created, p.createdby, p.updated, p.updatedby, p.c_bpartner_id, p.c_doctype_id, dt.signo_issotrx, dt.name AS doctypename, dt.printname AS doctypeprintname, p.documentno, p.issotrx, p.docstatus, p.datetrx, p.dateacct, p.c_currency_id, p.c_conversiontype_id, p.payamt AS amount, NULL::integer AS c_invoicepayschedule_id, p.duedate, p.datetrx AS truedatetrx, bp.socreditstatus, 0 as c_order_id, '
		|| selectallocationPayment || 
                  ' FROM c_payment p
              JOIN c_doctype dt ON p.c_doctype_id = dt.c_doctype_id
         JOIN c_bpartner bp ON p.c_bpartner_id = bp.c_bpartner_id AND (' || $1 || ' = -1 or p.c_bpartner_id = ' || $1 || ')
	LEFT JOIN c_allocationline al ON al.c_payment_id = p.c_payment_id 
	LEFT JOIN c_invoice il ON il.c_invoice_id = al.c_invoice_id
	LEFT JOIN M_BoletaDepositoLine bdlr on bdlr.c_reverse_payment_id = p.c_payment_id
	LEFT JOIN M_BoletaDeposito bdr on bdr.M_BoletaDeposito_ID = bdlr.M_BoletaDeposito_ID
	LEFT JOIN M_BoletaDepositoLine bdle on bdle.c_depo_payment_id = p.c_payment_id
	LEFT JOIN M_BoletaDeposito bde on bde.M_BoletaDeposito_ID = bdle.M_BoletaDeposito_ID
	LEFT JOIN M_BoletaDeposito bddb on bddb.c_boleta_payment_id = p.c_payment_id
  WHERE 
CASE
    WHEN il.ad_org_id IS NOT NULL AND il.ad_org_id <> p.ad_org_id THEN p.docstatus = ANY (ARRAY[''CO''::bpchar, ''CL''::bpchar])
    ELSE 1 = 1
END 

AND (CASE WHEN bdr.M_BoletaDeposito_ID IS NOT NULL 
		OR bde.M_BoletaDeposito_ID IS NOT NULL 
		OR bddb.M_BoletaDeposito_ID IS NOT NULL THEN p.docstatus NOT IN (''CO'',''CL'') 
	ELSE 1 = 1
	END) 

AND ' || whereclauseConditionCredit || '

' || orderby2 || '


)

UNION ALL

        ( SELECT DISTINCT ''C_CashLine''::text AS documenttable, cl.c_cashline_id AS document_id, cl.ad_client_id, COALESCE(il.ad_org_id, cl.ad_org_id) AS ad_org_id, cl.isactive, cl.created, cl.createdby, cl.updated, cl.updatedby,
                CASE
                    WHEN cl.c_bpartner_id IS NOT NULL THEN cl.c_bpartner_id
                    ELSE il.c_bpartner_id
                END AS c_bpartner_id, dt.c_doctype_id,
                CASE
                    WHEN cl.amount < 0.0 THEN 1
                    ELSE (-1)
                END AS signo_issotrx, dt.name AS doctypename, dt.printname AS doctypeprintname, ''@line@''::text || cl.line::character varying::text AS documentno,
                CASE
                    WHEN cl.amount < 0.0 THEN ''N''::bpchar
                    ELSE ''Y''::bpchar
                END AS issotrx, cl.docstatus, c.statementdate AS datetrx, c.dateacct, cl.c_currency_id, NULL::integer AS c_conversiontype_id, abs(cl.amount) AS amount, NULL::integer AS c_invoicepayschedule_id, NULL::timestamp without time zone AS duedate, c.statementdate AS truedatetrx, COALESCE(bp.socreditstatus, bp2.socreditstatus) AS socreditstatus, 0 as c_order_id, '
                || selectallocationCashline || 
       ' FROM c_cashline cl
      JOIN c_cash c ON cl.c_cash_id = c.c_cash_id
   LEFT JOIN c_bpartner bp ON cl.c_bpartner_id = bp.c_bpartner_id AND (' || $1 || ' = -1 or cl.c_bpartner_id = ' || $1 || ')
   JOIN ( SELECT d.ad_client_id, d.c_doctype_id, d.name, d.printname
         FROM c_doctype d
        WHERE d.doctypekey::text = ''CMC''::text) dt ON cl.ad_client_id = dt.ad_client_id
   LEFT JOIN c_allocationline al ON al.c_cashline_id = cl.c_cashline_id
   LEFT JOIN c_invoice il ON il.c_invoice_id = al.c_invoice_id AND (' || $1 || ' = -1 or il.c_bpartner_id = ' || $1 || ')
   LEFT JOIN c_bpartner bp2 ON il.c_bpartner_id = bp2.c_bpartner_id
  WHERE (CASE WHEN cl.c_bpartner_id IS NOT NULL THEN (' || $1 || ' = -1 or cl.c_bpartner_id = ' || $1 || ')
        WHEN il.c_bpartner_id IS NOT NULL THEN (' || $1 || ' = -1 or il.c_bpartner_id = ' || $1 || ')
        ELSE 1 = 2 END)
    AND (CASE WHEN il.ad_org_id IS NOT NULL AND il.ad_org_id <> cl.ad_org_id
        THEN cl.docstatus = ANY (ARRAY[''CO''::bpchar, ''CL''::bpchar])
        ELSE 1 = 1 END)

    AND ' || whereclauseConditionCredit || '

' || orderby3 || '

)) AS d  
WHERE ' || whereclauseDateTo || ' ; ';

-- raise notice '%', consulta;
FOR adocument IN EXECUTE consulta LOOP
	return next adocument;
END LOOP;

END
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION v_documents_org_filtered(integer, boolean, character, timestamp without time zone)
  OWNER TO libertya;

-- Función v_documents_org_filtered(integer, boolean, character)
CREATE OR REPLACE FUNCTION v_documents_org_filtered(
    bpartner integer,
    summaryonly boolean,
    condition character)
  RETURNS SETOF v_documents_org_type_condition AS
$BODY$
BEGIN
	return query select * from v_documents_org_filtered(bpartner, summaryonly, condition, null::timestamp);
END
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION v_documents_org_filtered(integer, boolean, character)
  OWNER TO libertya;