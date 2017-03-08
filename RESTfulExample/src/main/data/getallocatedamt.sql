--20160905-1920 Nuevas y modificaciones de funciones que dan soporte a las correcciones de cuenta corriente
-- Función getallocatedamt(integer, integer, integer, integer, timestamp without time zone, integer)
CREATE OR REPLACE FUNCTION getallocatedamt(
    p_c_invoice_id integer,
    p_c_currency_id integer,
    p_c_conversiontype_id integer,
    p_multiplierap integer,
    p_fechacorte timestamp without time zone,
    p_c_invoicepayschedule_id integer)
  RETURNS numeric AS
$BODY$ 
DECLARE
	v_MultiplierAP		NUMERIC := 1;
	v_PaidAmt			NUMERIC := 0;
	v_ConversionType_ID INTEGER := p_c_conversionType_ID;
	v_Currency_ID       INTEGER := p_c_currency_id;
	v_Temp     NUMERIC;
	v_SchedulesAmt NUMERIC;
	v_Diff NUMERIC;
	ar			RECORD;
	s			RECORD;
	v_DateAcct timestamp without time zone;
	schedule_founded boolean;
BEGIN
	--	Default
	IF (p_MultiplierAP IS NOT NULL) THEN
		v_MultiplierAP := p_MultiplierAP::numeric;
	END IF;
	
	SELECT DateAcct
	       INTO v_DateAcct
	FROM C_Invoice 
	WHERE C_Invoice_ID = p_c_invoice_id;

	FOR ar IN 
		SELECT	a.AD_Client_ID, a.AD_Org_ID,
		al.Amount, al.DiscountAmt, al.WriteOffAmt,
		a.C_Currency_ID, a.DateTrx , al.C_Invoice_Credit_ID
		FROM	C_AllocationLine al
		INNER JOIN C_AllocationHdr a ON (al.C_AllocationHdr_ID=a.C_AllocationHdr_ID)
		WHERE	(al.C_Invoice_ID = p_C_Invoice_ID OR 
				al.C_Invoice_Credit_ID = p_C_Invoice_ID ) -- condicion no en Adempiere
          	AND   a.IsActive='Y'
          	AND   (p_fechacorte is null OR a.dateacct::date <= p_fechacorte::date)
	LOOP
	    -- Agregado, para facturas como pago
		IF (p_C_Invoice_ID = ar.C_Invoice_Credit_ID) THEN
		   v_Temp := ar.Amount;
		ELSE
		   v_Temp := ar.Amount + ar.DisCountAmt + ar.WriteOffAmt;
		END IF;
		-- Se asume que este v_Temp es no negativo
		v_PaidAmt := v_PaidAmt
        -- Allocation
			+ currencyConvert(v_Temp,
				ar.C_Currency_ID, v_Currency_ID, v_DateAcct, v_ConversionType_ID, 
				ar.AD_Client_ID, ar.AD_Org_ID);

	--RAISE NOTICE ' C_Invoice_ID=% , PaidAmt=% , Allocation= % ',p_C_Invoice_ID, v_PaidAmt, v_Temp;
	END LOOP;

	--Si existe un payschedule del comprobante como parametro, entonces se devuelve el importe imputado de ese payschedule
	IF (p_c_invoicepayschedule_id > 0) THEN 
		v_SchedulesAmt := 0;
		schedule_founded := false;        
		FOR s IN  SELECT  ips.C_InvoicePaySchedule_ID, currencyConvert(ips.DueAmt, i.c_currency_id, v_Currency_ID, v_DateAcct, v_ConversionType_ID, i.AD_Client_ID, i.AD_Org_ID) as DueAmt 	        
			FROM    C_InvoicePaySchedule ips 	        
			INNER JOIN C_Invoice i on (ips.C_Invoice_ID = i.C_Invoice_ID) 		
			WHERE	ips.C_Invoice_ID = p_c_invoice_id AND   ips.IsValid='Y'         	
			ORDER BY ips.DueDate 
		LOOP    
			-- Acumulo los importes de cada schedule hasta llegar al c_invoicepayschedule_id parámetro
			v_SchedulesAmt := v_SchedulesAmt + s.DueAmt;
			schedule_founded := s.C_InvoicePaySchedule_ID = p_c_invoicepayschedule_id;
			IF (schedule_founded) THEN
				-- Si llegamos al parámetro, entonces se le resta el acumulado de schedules a lo imputado
				v_Diff := v_PaidAmt - v_SchedulesAmt;
				-- Si el importe resultante es:
				-- 1) >= 0: Significa que imputado hay mas que el acumulado, entonces lo imputado es el total de la cuota
				IF (v_Diff >= 0) THEN
					v_PaidAmt := s.DueAmt;
				ELSE
					-- 2) < 0: Significa que hay imputado algo o nada de la cuota 
					-- Al importe de la cuota, se le resta la diferencia anterior absoluta
					v_PaidAmt := s.DueAmt - abs(v_Diff);
					-- Si la diferencia es menor o igual a 0, significa que no hay nada imputado
					-- Caso contrario, lo pagado es dicha diferencia
					IF (v_PaidAmt <= 0) THEN
						v_PaidAmt := 0;
					END IF;
				END IF;
				EXIT;
			END IF;
		END LOOP;
		-- Si no se encontró el schedule, entonces el imputado es 0
		IF (NOT schedule_founded) THEN
			v_PaidAmt := 0;
		END IF;
	END IF;
	
	RETURN	v_PaidAmt * v_MultiplierAP;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION getallocatedamt(integer, integer, integer, integer, timestamp without time zone, integer)
  OWNER TO libertya;

-- Función getallocatedamt(integer, integer, integer, integer, timestamp without time zone)
CREATE OR REPLACE FUNCTION getallocatedamt(
    p_c_invoice_id integer,
    p_c_currency_id integer,
    p_c_conversiontype_id integer,
    p_multiplierap integer,
    p_fechacorte timestamp without time zone)
  RETURNS numeric AS
$BODY$ 
BEGIN
	RETURN getallocatedamt(p_c_invoice_id, p_c_currency_id, p_c_conversiontype_id, p_multiplierap, p_fechacorte, 0);
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION getallocatedamt(integer, integer, integer, integer, timestamp without time zone)
  OWNER TO libertya;

