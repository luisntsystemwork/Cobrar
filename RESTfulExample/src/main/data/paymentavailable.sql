
-- Función paymentavailable(integer, timestamp)
CREATE OR REPLACE FUNCTION paymentavailable(p_c_payment_id integer, dateTo timestamp)
  RETURNS numeric AS
$BODY$
DECLARE
	v_Currency_ID		INTEGER;
	v_AvailableAmt		NUMERIC := 0;
   	v_IsReceipt         CHARACTER(1);
   	v_Amt               NUMERIC := 0;
   	r   			RECORD;
	v_Charge_ID INTEGER; 
	v_ConversionType_ID INTEGER; 
	
	v_DateAcct timestamp without time zone;
BEGIN
	BEGIN
	
	SELECT	C_Currency_ID, PayAmt, IsReceipt, 
			C_Charge_ID,C_ConversionType_ID, DateAcct
	  INTO	STRICT 
			v_Currency_ID, v_AvailableAmt, v_IsReceipt,
			v_Charge_ID,v_ConversionType_ID, v_DateAcct
	FROM	C_Payment     
	WHERE	C_Payment_ID = p_C_Payment_ID;
		EXCEPTION	
		WHEN OTHERS THEN
            	RAISE NOTICE 'PaymentAvailable - %', SQLERRM;
			RETURN NULL;
	END;
	
	IF (v_Charge_ID > 0 ) THEN 
	   RETURN 0;
	END IF;
	
	FOR r IN
		SELECT	a.AD_Client_ID, a.AD_Org_ID, al.Amount, a.C_Currency_ID, a.DateTrx
		FROM	C_AllocationLine al
	        INNER JOIN C_AllocationHdr a ON (al.C_AllocationHdr_ID=a.C_AllocationHdr_ID)
		WHERE	al.C_Payment_ID = p_C_Payment_ID
          	AND   a.IsActive='Y'
          	AND (dateTo IS NULL OR a.dateacct::date <= dateTo::date)
	LOOP
        v_Amt := currencyConvert(r.Amount, r.C_Currency_ID, v_Currency_ID, 
				v_DateAcct, v_ConversionType_ID, r.AD_Client_ID, r.AD_Org_ID);
	    v_AvailableAmt := v_AvailableAmt - v_Amt;
	END LOOP;
	
	IF (v_AvailableAmt < 0) THEN
		RAISE NOTICE 'Payment Available negative, correcting to zero - %',v_AvailableAmt ;
		v_AvailableAmt := 0;
	END IF;
	
	v_AvailableAmt :=  currencyRound(v_AvailableAmt,v_Currency_ID,NULL);
	RETURN	v_AvailableAmt;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION paymentavailable(integer, timestamp)
  OWNER TO libertya;

-- Función paymentavailable(integer)
CREATE OR REPLACE FUNCTION paymentavailable(p_c_payment_id integer)
  RETURNS numeric AS
$BODY$
BEGIN
	RETURN paymentavailable(p_c_payment_id, null::timestamp);
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION paymentavailable(integer)
  OWNER TO libertya;