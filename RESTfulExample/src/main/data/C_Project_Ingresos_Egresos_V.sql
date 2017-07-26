-- DROP VIEW libertya.c_project_ingresos_egresos_v;
-- SELECT * FROM  libertya.c_project_ingresos_egresos_v


 CREATE OR REPLACE VIEW libertya.C_Project_Ingresos_Egresos_V AS 

-- Incluye los Comprobantes En otra Moneda

SELECT 	'INGRE' AS "IngresoEgreso",
	I.ad_Client_ID, I.ad_Org_ID,
	NULLIF(i.c_Project_Id, 901) as "Project_ID", 
	PR.Value AS "CODIGO_Proyecto",PR.Name AS "Nombre_Proyecto", BP.Value as "Cliente", BP.Name as "RazonSocial", 
	Mo.Iso_Code as "Mone_Comprob", mopr.iso_code as "Mone_Proj",
	rt.multiplyrate as "CotMulti",
	rt.DivideRate as "CotDiv", 
	DT.PrintName as "Tipo_Comprobante", 
	i.documentno as "Nro_Comprobante ", 
	To_Char(dateinvoiced, 'YYYY/MM/DD') AS "FECHA", 
	p.Name as "Concepto",  
	i.c_currency_id,  
	((LineTotalAmt * signo_issotrx)) 			as ImporteMO,
	((LineTotalAmt * signo_issotrx) * rt.multiplyrate) 	as Importe,  
 	((LineTotalAmt * signo_issotrx) ) 			as ImporteHBL
 FROM libertya.c_invoice I 
  INNER JOIN libertya.c_invoiceline IL 		ON (i.c_invoice_id 		= IL.c_Invoice_id)
  INNER JOIN libertya.c_bpartner BP 		ON (I.c_bpartner_id 		= BP.c_bpartner_id)
  LEFT  JOIN libertya.m_product p 		ON (p.m_product_id 		= il.m_product_id)
  INNER JOIN libertya.C_doctype DT 		ON (i.c_doctypetarget_id 	= Dt.C_doctype_id)
  LEFT  JOIN libertya.C_Project PR 		ON (i.c_Project_id 		= PR.C_Project_Id) 
  INNER JOIN libertya.c_currency MO 		ON (I.c_currency_id 		= MO.c_currency_id)
  INNER JOIN libertya.c_currency MOPR 		ON (PR.c_currency_id 		= MOPR.c_currency_id)
  LEFT  JOIN libertya.c_conversion_rate rt 	ON (i.c_currency_id 		= rt.c_currency_id 
							AND (rt.C_currency_id_to = 118 		AND rt.c_currency_id <> 118) 
							AND (i.dateinvoiced >= rt.validfrom 	and i.dateinvoiced <= rt.validto)) 

  
  where I.isactive = 'Y'
  and i.issotrx = 'Y'
  and I.c_currency_id <> 118
 --and i.c_Project_Id = 1010022

  UNION 

-- Agrega Los Comprobantes en ARS

  SELECT 	'INGRE' AS "IngresoEgreso",
	I.ad_Client_ID, I.ad_Org_ID,
	NULLIF(i.c_Project_Id, 901) as "Project_ID", 
	PR.Value AS "CODIGO_Proyecto",PR.Name AS "Nombre_Proyecto", BP.Value as "Cliente", BP.Name as "RazonSocial", 
	Mo.Iso_Code as "Mone_Comprob", mopr.iso_code as "Mone_Proj",
	1 as "CotMulti",
	rtDol.multiplyrate as "CotDiv", 
	DT.PrintName as "Tipo_Comprobante", 
	i.documentno as "Nro_Comprobante ", 
	To_Char(dateinvoiced, 'YYYY/MM/DD') AS "FECHA", 
	p.Name as "Concepto",  
	i.c_currency_id,  
	((LineTotalAmt * signo_issotrx)) 					as ImporteMO,
	((LineTotalAmt * signo_issotrx)) 					as Importe, 
 	ROUND(((LineTotalAmt * (signo_issotrx )) / rtdol.multiplyrate), 2) 	as ImporteHBL
 FROM libertya.c_invoice I 
  INNER JOIN libertya.c_invoiceline IL 		ON (i.c_invoice_id 		= IL.c_Invoice_id)
  INNER JOIN libertya.c_bpartner BP 		ON (I.c_bpartner_id 		= BP.c_bpartner_id)
  LEFT  JOIN libertya.m_product p 		ON (p.m_product_id 		= il.m_product_id)
  INNER JOIN libertya.C_doctype DT 		ON (i.c_doctypetarget_id 	= Dt.C_doctype_id)
  LEFT  JOIN libertya.C_Project PR 		ON (i.c_Project_id 		= PR.C_Project_Id) 
  INNER JOIN libertya.c_currency MO 		ON (I.c_currency_id 		= MO.c_currency_id)
  INNER JOIN libertya.c_currency MOPR 		ON (PR.c_currency_id 		= MOPR.c_currency_id)
  LEFT  JOIN libertya.c_conversion_rate rt 	ON (i.c_currency_id 		= rt.c_currency_id 
							AND ( rt.c_currency_id = 118 AND rt.C_currency_id_to <> 118) 
							AND (i.dateinvoiced >= rt.validfrom 	and i.dateinvoiced <= rt.validto)) 
  LEFT  JOIN libertya.c_conversion_rate rtDol 	ON (rtdol.c_currency_id = 100
							AND (i.dateinvoiced >= rtDol.validfrom 	and i.dateinvoiced <= rtDol.validto)) 

  
  where I.isactive = 'Y'
  and i.issotrx = 'Y'
  and I.c_currency_id = 118
 -- and i.c_Project_Id = 1010022



UNION 


-- Agrega Las Compras en otra Moneda

SELECT 	'EGRE' AS "IngresoEgreso",
	I.ad_Client_ID, I.ad_Org_ID,
	NULLIF(i.c_Project_Id, 901) as "Project_ID", 
	PR.Value AS "CODIGO_Proyecto",PR.Name AS "Nombre_Proyecto", BP.Value as "Cliente", BP.Name as "RazonSocial", 
	Mo.Iso_Code as "Mone_Comprob", mopr.iso_code as "Mone_Proj",
	rt.multiplyrate as "CotMulti",
	rt.DivideRate as "CotDiv", 
	DT.PrintName as "Tipo_Comprobante", 
	i.documentno as "Nro_Comprobante ", 
	To_Char(dateinvoiced, 'YYYY/MM/DD') AS "FECHA", 
	p.Name as "Concepto",  
	i.c_currency_id,  
	((LineTotalAmt * (signo_issotrx * -1))) 			as ImporteMO,
	((LineTotalAmt * (signo_issotrx * -1)) * rt.multiplyrate) 	as Importe,  
 	((LineTotalAmt * (signo_issotrx * -1)) ) 			as ImporteHBL
 FROM libertya.c_invoice I 
  INNER JOIN libertya.c_invoiceline IL 		ON (i.c_invoice_id 		= IL.c_Invoice_id)
  INNER JOIN libertya.c_bpartner BP 		ON (I.c_bpartner_id 		= BP.c_bpartner_id)
  LEFT  JOIN libertya.m_product p 		ON (p.m_product_id 		= il.m_product_id)
  INNER JOIN libertya.C_doctype DT 		ON (i.c_doctypetarget_id 	= Dt.C_doctype_id)
  LEFT  JOIN libertya.C_Project PR 		ON (i.c_Project_id 		= PR.C_Project_Id) 
  INNER JOIN libertya.c_currency MO 		ON (I.c_currency_id 		= MO.c_currency_id)
  INNER JOIN libertya.c_currency MOPR 		ON (PR.c_currency_id 		= MOPR.c_currency_id)
 LEFT  JOIN libertya.c_conversion_rate rt 	ON (i.c_currency_id 		= rt.c_currency_id 
							AND (rt.C_currency_id_to = 118 		AND rt.c_currency_id <> 118) 
							AND (i.dateinvoiced >= rt.validfrom 	and i.dateinvoiced <= rt.validto)) 

  
  where I.isactive = 'Y'
  and i.issotrx = 'N'
  and I.c_currency_id <> 118
 -- and i.c_Project_Id = 1010022

  UNION 


-- Agrega las Compras en Moneda ARS


  SELECT 	'EGRE' AS "IngresoEgreso",
  	I.ad_Client_ID, I.ad_Org_ID,
	NULLIF(i.c_Project_Id, 901) as "Project_ID", 
	PR.Value AS "CODIGO_Proyecto",PR.Name AS "Nombre_Proyecto", BP.Value as "Cliente", BP.Name as "RazonSocial", 
	Mo.Iso_Code as "Mone_Comprob", mopr.iso_code as "Mone_Proj",
	1 as "CotMulti",
	rtDol.multiplyrate as "CotDiv", 
	DT.PrintName as "Tipo_Comprobante", 
	i.documentno as "Nro_Comprobante ", 
	To_Char(dateinvoiced, 'YYYY/MM/DD') AS "FECHA", 
	p.Name as "Concepto",  
	i.c_currency_id,  
	((LineTotalAmt * (signo_issotrx * -1))) 				as ImporteMO,
	((LineTotalAmt * (signo_issotrx * -1))) 				as Importe,  
 	ROUND(((LineTotalAmt * (signo_issotrx * -1)) / rtdol.multiplyrate), 2) 	as ImporteHBL
 FROM libertya.c_invoice I 
  INNER JOIN libertya.c_invoiceline IL 		ON (i.c_invoice_id 		= IL.c_Invoice_id)
  INNER JOIN libertya.c_bpartner BP 		ON (I.c_bpartner_id 		= BP.c_bpartner_id)
  LEFT  JOIN libertya.m_product p 		ON (p.m_product_id 		= il.m_product_id)
  INNER JOIN libertya.C_doctype DT 		ON (i.c_doctypetarget_id 	= Dt.C_doctype_id)
  LEFT  JOIN libertya.C_Project PR 		ON (i.c_Project_id 		= PR.C_Project_Id) 
  INNER JOIN libertya.c_currency MO 		ON (I.c_currency_id 		= MO.c_currency_id)
  INNER JOIN libertya.c_currency MOPR 		ON (PR.c_currency_id 		= MOPR.c_currency_id)
  LEFT  JOIN libertya.c_conversion_rate rt 	ON (i.c_currency_id 		= rt.c_currency_id 
							AND ( rt.c_currency_id = 118) 
							AND (i.dateinvoiced >= rt.validfrom 	and i.dateinvoiced <= rt.validto)) 
  LEFT  JOIN libertya.c_conversion_rate rtDol 	ON (rtdol.c_currency_id = 100
							AND (i.dateinvoiced >= rtDol.validfrom 	and i.dateinvoiced <= rtDol.validto)) 

  
  where I.isactive = 'Y'
  and i.issotrx = 'N'
  and I.c_currency_id = 118
--  and i.c_Project_Id = 1010022


UNION


-- Agrega los Pedidos No Facturados en Otra Moneda


SELECT 	'EGRE' AS "IngresoEgreso",
	I.ad_Client_ID, I.ad_Org_ID,
	NULLIF(i.c_Project_Id, 901) as "Project_ID", 
	PR.Value AS "CODIGO_Proyecto",PR.Name AS "Nombre_Proyecto", BP.Value as "Cliente", BP.Name as "RazonSocial", 
	Mo.Iso_Code as "Mone_Comprob", mopr.iso_code as "Mone_Proj",
	rt.multiplyrate as "CotMulti",
	rt.DivideRate as "CotDiv", 
	DT.PrintName as "Tipo_Comprobante", 
	i.documentno as "Nro_Comprobante ", 
	To_Char(I.DateOrdered, 'YYYY/MM/DD') AS "FECHA", 
	p.Name as "Concepto",  
	i.c_currency_id,  
	((LineTotalAmt * (signo_issotrx * -1))) 			as ImporteMO,
	((LineTotalAmt * (signo_issotrx * -1)) * rt.multiplyrate) 	as Importe,  
 	((LineTotalAmt * (signo_issotrx * -1)) ) 			as ImporteHBL


 FROM libertya.c_Order I 
  INNER JOIN libertya.c_orderline IL ON I.c_order_id = IL.c_order_id AND IL.qtyordered <> IL.qtyinvoiced AND (I.docstatus = ANY (ARRAY['CO'::bpchar, 'CL'::bpchar])) AND 

IL.m_product_id IS NOT NULL
  INNER JOIN libertya.c_bpartner BP 		ON (I.c_bpartner_id 		= BP.c_bpartner_id)
  LEFT  JOIN libertya.m_product p 		ON (p.m_product_id 		= il.m_product_id)
  INNER JOIN libertya.C_doctype DT 		ON (i.c_doctypetarget_id 	= Dt.C_doctype_id)
  LEFT  JOIN libertya.C_Project PR 		ON (i.c_Project_id 		= PR.C_Project_Id) 
  INNER JOIN libertya.c_currency MO 		ON (I.c_currency_id 		= MO.c_currency_id)
  INNER JOIN libertya.c_currency MOPR 		ON (PR.c_currency_id 		= MOPR.c_currency_id)
  LEFT  JOIN libertya.c_conversion_rate rt 	ON (i.c_currency_id 		= rt.c_currency_id 
							AND (rt.C_currency_id_to = 118 		AND rt.c_currency_id <> 118) 
							AND (i.dateordered >= rt.validfrom 	and i.dateordered <= rt.validto)) 

  
  where I.isactive = 'Y'
  and i.issotrx = 'N'
  and I.c_currency_id <> 118
 -- and i.c_Project_Id = 1010022

  UNION

  -- Agrega Los Pedidos No Facturados en ARS

  SELECT 	'EGRE' AS "IngresoEgreso",
  	I.ad_Client_ID, I.ad_Org_ID,
	NULLIF(i.c_Project_Id, 901) as "Project_ID", 
	PR.Value AS "CODIGO_Proyecto", PR.Name AS "Nombre_Proyecto", BP.Value as "Cliente", BP.Name as "RazonSocial", 
	Mo.Iso_Code as "Mone_Comprob", mopr.iso_code as "Mone_Proj",
	1 as "CotMulti",
	rtDol.multiplyrate as "CotDiv", 
	DT.PrintName as "Tipo_Comprobante", 
	i.documentno as "Nro_Comprobante ", 
	To_Char(I.DateOrdered, 'YYYY/MM/DD') AS "FECHA", 
	p.Name as "Concepto",  
	i.c_currency_id,  
	((LineTotalAmt * (signo_issotrx * -1))) 				as ImporteMO,
	((LineTotalAmt * (signo_issotrx * -1))) 				as Importe,  
 	ROUND(((LineTotalAmt * (signo_issotrx * -1)) / rtdol.multiplyrate), 2) 	as ImporteHBL
 FROM libertya.c_Order I 
  INNER JOIN libertya.c_orderline IL ON I.c_order_id = IL.c_order_id AND IL.qtyordered <> IL.qtyinvoiced AND (I.docstatus = ANY (ARRAY['CO'::bpchar, 'CL'::bpchar])) AND 

IL.m_product_id IS NOT NULL
  INNER JOIN libertya.c_bpartner BP 		ON (I.c_bpartner_id 		= BP.c_bpartner_id)
  LEFT  JOIN libertya.m_product p 		ON (p.m_product_id 		= il.m_product_id)
  INNER JOIN libertya.C_doctype DT 		ON (i.c_doctypetarget_id 	= Dt.C_doctype_id)
  LEFT  JOIN libertya.C_Project PR 		ON (i.c_Project_id 		= PR.C_Project_Id) 
  INNER JOIN libertya.c_currency MO 		ON (I.c_currency_id 		= MO.c_currency_id)
  INNER JOIN libertya.c_currency MOPR 		ON (PR.c_currency_id 		= MOPR.c_currency_id)
  INNER JOIN libertya.c_currency MODol 		ON (MODol.c_currency_id 	= 100)
  LEFT  JOIN libertya.c_conversion_rate rt 	ON (i.c_currency_id 		= rt.c_currency_id 
							AND ( rt.c_currency_id = 118 AND rt.C_currency_id_to <> 118) 
							AND (i.dateordered >= rt.validfrom 	and i.dateordered <= rt.validto)) 
  LEFT  JOIN libertya.c_conversion_rate rtDol 	ON (rtdol.c_currency_id = 100
							AND (i.dateordered >= rtDol.validfrom 	and i.dateordered <= rtDol.validto)) 


  
  where I.isactive = 'Y'
  and i.issotrx = 'N'
  and I.c_currency_id = 118 -- IdMonARS
 -- and i.c_Project_Id = 1010022

  UNION

  -- Agrega Los Remitos No Facturados en ARS


SELECT 	'EGRE' AS "IngresoEgreso", 
	i.ad_client_id, 
	i.ad_org_id, 
	NULLIF(i.c_project_id, 901) AS "Project_ID", 
	pr.value AS "CODIGO_Proyecto", pr.name AS "Nombre_Proyecto", 
	bp.value AS "Cliente", bp.name AS "RazonSocial", 
	mo.iso_code AS "Mone_Comprob", 
	mopr.iso_code AS "Mone_Proj", 
	1 AS "CotMulti", 
	rtdol.multiplyrate AS "CotDiv", 
	dt.printname AS "Tipo_Comprobante", 
	io.documentno AS "Nro_Comprobante ", 
	to_char(i.dateordered, 'YYYY/MM/DD'::text) AS "FECHA",
	p.name AS "Concepto", 
	i.c_currency_id, 
	iol.movementqty * (dt.signo_issotrx * (-1))::numeric AS importemo, 
	iol.movementqty * (dt.signo_issotrx * (-1))::numeric AS importe, 
	round(iol.movementqty * (dt.signo_issotrx * (-1))::numeric / rtdol.multiplyrate, 2) AS importehbl
	
	FROM libertya.m_inoutline iol
	INNER JOIN libertya.m_inout io 			ON iol.m_inout_id 		= io.m_inout_id
        INNER JOIN libertya.c_order i  			ON i.c_order_Id 		= io.C_Order_Id
	LEFT  JOIN libertya.m_product p 		ON p.m_product_id 		= iol.m_product_id
	INNER JOIN libertya.c_doctype dt 		ON io.c_doctype_id	 	= dt.c_doctype_id
	INNER JOIN libertya.c_currency mo 		ON i.c_currency_id 		= mo.c_currency_id
	INNER JOIN libertya.c_currency modol 		ON modol.c_currency_id 		= 100
	INNER JOIN libertya.c_project pr 		ON i.c_project_id 		= pr.c_project_id
	INNER JOIN libertya.c_currency mopr 		ON pr.c_currency_id 		= mopr.c_currency_id
	INNER JOIN libertya.c_bpartner bp 		ON i.c_bpartner_id 		= bp.c_bpartner_id
	LEFT  JOIN libertya.c_conversion_rate rt 	ON i.c_currency_id 		= rt.c_currency_id 	AND rt.c_currency_id 	= 118 AND rt.c_currency_id_to 	<> 118 AND i.dateordered >= rt.validfrom AND i.dateordered <= rt.validto
	LEFT  JOIN libertya.c_conversion_rate rtdol 	ON rtdol.c_currency_id 		= 100 			AND i.dateordered 	>= rtdol.validfrom AND i.dateordered <= rtdol.validto
	LEFT  JOIN ( SELECT minv.m_inoutline_id, sum(minv.qty) AS qty
		   FROM libertya.m_matchinv minv
		   JOIN libertya.c_invoiceline il 	ON il.c_invoiceline_id 	= minv.c_invoiceline_id
		   JOIN libertya.c_invoice i 		ON i.c_invoice_id 	= il.c_invoice_id
		   JOIN libertya.c_doctype dti 		ON dti.c_doctype_id 	= i.c_doctype_id
		   JOIN libertya.m_inoutline iol 	ON iol.m_inoutline_id 	= minv.m_inoutline_id
		   JOIN libertya.m_inout io 		ON io.m_inout_id 	= iol.m_inout_id
		   JOIN libertya.c_doctype dtio 	ON dtio.c_doctype_id 	= io.c_doctype_id
		  WHERE dti.docbasetype = 'API'::bpchar AND dtio.docbasetype 	= 'MMR'::bpchar AND dtio.signo_issotrx = 1
		  GROUP BY minv.m_inoutline_id) qryCant ON iol.m_inoutline_id 	= qryCant.m_inoutline_id

	WHERE  iol.ad_client_Id = 1010016   
		AND  qryCant.qty IS NULL 
  		AND I.c_currency_id = 118 -- IdMonARS
		AND io.isactive = 'Y'::bpchar  
		AND (io.docstatus = ANY (ARRAY['CL'::bpchar, 'CO'::bpchar])) AND dt.docbasetype = 'MMR'::bpchar AND dt.signo_issotrx = 1

  UNION

  -- Agrega Los Remitos No Facturados en Otra Moneda

SELECT DISTINCT
	'EGRE' AS "IngresoEgreso", i.ad_client_id, 
	i.ad_org_id,  
	NULLIF(i.c_project_id, 901) AS "Project_ID", 
	pr.value AS "CODIGO_Proyecto", pr.name AS "Nombre_Proyecto", 
	bp.value AS "Cliente", bp.name AS "RazonSocial", 
	mo.iso_code AS "Mone_Comprob", 
	mopr.iso_code AS "Mone_Proj", 
	rt.multiplyrate as "CotMulti",
	rt.DivideRate as "CotDiv", 
	dt.printname AS "Tipo_Comprobante", 
	io.documentno AS "Nro_Comprobante ", 
	to_char(i.dateordered, 'YYYY/MM/DD'::text) AS "FECHA",
	p.name AS "Concepto", 
	i.c_currency_id, iol.movementqty * (dt.signo_issotrx * (-1))::numeric AS importemo, 
	iol.movementqty * (dt.signo_issotrx * (-1))::numeric AS importe, round(iol.movementqty * (dt.signo_issotrx * (-1))::numeric / rt.multiplyrate, 2) AS importehbl
	
	FROM libertya.m_inoutline iol
	INNER JOIN libertya.m_inout io 			ON iol.m_inout_id 		= io.m_inout_id
        INNER JOIN libertya.c_order i  			ON i.c_order_Id 		= io.C_Order_Id
	LEFT  JOIN libertya.m_product p 		ON p.m_product_id 		= iol.m_product_id
	INNER JOIN libertya.c_doctype dt 		ON io.c_doctype_id	 	= dt.c_doctype_id
	INNER JOIN libertya.c_currency mo 		ON i.c_currency_id 		= mo.c_currency_id
	INNER JOIN libertya.c_currency modol 		ON modol.c_currency_id 		= 100
	INNER JOIN libertya.c_project pr 		ON i.c_project_id 		= pr.c_project_id
	INNER JOIN libertya.c_currency mopr 		ON pr.c_currency_id 		= mopr.c_currency_id
	INNER JOIN libertya.c_bpartner bp 		ON i.c_bpartner_id 		= bp.c_bpartner_id
  	LEFT  JOIN libertya.c_conversion_rate rt 	ON (i.c_currency_id 		= rt.c_currency_id 
							AND (rt.C_currency_id_to = 118 		AND rt.c_currency_id <> 118) 
							AND (i.dateordered >= rt.validfrom 	and i.dateordered <= rt.validto)) 
	LEFT  JOIN ( SELECT minv.m_inoutline_id, sum(minv.qty) AS qty
		   FROM libertya.m_matchinv minv
		   JOIN libertya.c_invoiceline il 	ON il.c_invoiceline_id 	= minv.c_invoiceline_id
		   JOIN libertya.c_invoice i 		ON i.c_invoice_id 	= il.c_invoice_id
		   JOIN libertya.c_doctype dti 		ON dti.c_doctype_id 	= i.c_doctype_id
		   JOIN libertya.m_inoutline iol 	ON iol.m_inoutline_id 	= minv.m_inoutline_id
		   JOIN libertya.m_inout io 		ON io.m_inout_id 	= iol.m_inout_id
		   JOIN libertya.c_doctype dtio 	ON dtio.c_doctype_id 	= io.c_doctype_id
		  WHERE dti.docbasetype = 'API'::bpchar AND dtio.docbasetype 	= 'MMR'::bpchar AND dtio.signo_issotrx = 1
		  GROUP BY minv.m_inoutline_id) qryCant ON iol.m_inoutline_id 	= qryCant.m_inoutline_id

	WHERE  iol.ad_client_Id = 1010016   
		AND  qryCant.qty IS NULL 
  		AND I.c_currency_id <> 118 -- Distinto IdMonARS
		AND io.isactive = 'Y'::bpchar  
		AND (io.docstatus = ANY (ARRAY['CL'::bpchar, 'CO'::bpchar])) AND dt.docbasetype = 'MMR'::bpchar AND dt.signo_issotrx = 1


  ORDER BY "Project_ID", "IngresoEgreso", "Mone_Comprob";


 ALTER TABLE Libertya.C_Project_Ingresos_Egresos_V OWNER TO libertya;