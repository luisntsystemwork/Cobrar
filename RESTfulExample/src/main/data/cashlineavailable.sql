
-- Función cashlineavailable(integer, timestamp)
CREATE OR REPLACE FUNCTION cashlineavailable(
    p_c_cashline_id integer,
    p_date_to timestamp)
  RETURNS numeric AS
$BODY$
/*************************************************************************
-Retorna NULL si parametro es null o si la linea no existe
-Retorna la cantidad disponible de la linea para alocacion futuras usando el mismo signo 
 que la linea, esto es, si C_CashLine.Amt <0 , se retorna 0 o un numero
 negativo; si C_CashLine.amt >0 , se retrona cero o un numero positivo.
-la cantidad disponible inicial de una linea de caja es C_CashLine.Amt
 (esto es, no se tiene en cuenta ni C_CashLine.DiscountAmt ni 
 C_CashLine.WriteoffAmt) 
-asume que las alocaciones son no negativas y solo se consideran aquellas
 lineas de alocacion que pertenecen a una cabecera de alocacion (C_AllocationHdr)
 activa (esta es la unica condicion que se aplica)
- se considera como monto de alocacion con respecto a la linea de caja 
  a C_AllocationLine.Amount (esto es, no se tiene en cuenta C_AllocationLine.WriteOff ni
  C_AllocationLine.Discount)
  
TEST: 
-- montos de lienas, monto disponible, y alocaciones relacionadas cada una de las lineas de caja
-- Availabe DEBE ser cero o tener el mismo signo que Amount,
-- si se usa una sola moneda, entonces 
-- (suma de AmountAllocatedInAlocLine en AH activas) + ABS(Available) debe ser iugal a  ABS(Amoumt) 
select cl.c_cashLine_id,cl.amount, 
cashLineAvailable(cl.c_cashLine_id) as Available
,al.c_allocationLine_id ,
al.amount as AmountAllocatedInAlocLine,
cl.c_currency_id as currencyCashLine,
ah.c_currency_id as currencyAlloc,
ah.isActive as AHActive
from 
c_cashLine cl left join c_allocationLine al on
  (al.c_cashLine_id = cl.c_cashLine_id)
left join 
C_AllocationHDR ah on (ah.C_allocationHdr_id = al.C_allocationHdr_id)

order by cl.c_cashLine_id;
  
************************************************************************/
DECLARE
	v_Currency_ID		INTEGER;
	v_Amt               NUMERIC;
   	r   			RECORD;
	v_ConversionType_ID INTEGER := 0; -- actuamente, tal como en PL/java se usa siempre 0, no se toma desde cashLine
	v_allocation NUMERIC;
	v_allocatedAmt NUMERIC;	-- candida alocada total convertida a la moneda de la linea 
	v_AvailableAmt		NUMERIC := 0;
	v_DateAcct timestamp without time zone;
 
BEGIN
	IF (p_C_Cashline_id IS NULL OR p_C_Cashline_id = 0) THEN
		RETURN NULL;
	END IF;
	
	--	Get Currency and Amount
	SELECT	C_Currency_ID, Amount
		INTO v_Currency_ID, v_Amt
	FROM	C_CashLine    
	WHERE	C_CashLine_ID  = p_C_Cashline_id;

	SELECT DateAcct
	       INTO v_DateAcct
	FROM C_Cash c 
	INNER JOIN C_CashLine cl ON c.C_Cash_ID = cl.C_Cash_ID 
	WHERE C_CashLine_ID = p_C_Cashline_id;
	
	IF NOT FOUND THEN
	  RETURN NULL;
	END IF;
	
	-- Calculate Allocated Amount
	-- input: p_C_Cashline_id,v_Currency_ID,v_ConversionType_ID
	--output: v_allocatedAmt
	v_allocatedAmt := 0.00;
	FOR r IN
		SELECT	a.AD_Client_ID, a.AD_Org_ID, al.Amount, a.C_Currency_ID, a.DateTrx
		FROM	C_AllocationLine al
	        INNER JOIN C_AllocationHdr a ON (al.C_AllocationHdr_ID=a.C_AllocationHdr_ID)
		WHERE	al.C_CashLine_ID = p_C_Cashline_id
          	AND   a.IsActive='Y'
          	AND (p_date_to IS NULL OR a.dateacct::date <= p_date_to::date)
	LOOP
        v_allocation := currencyConvert(r.Amount, r.C_Currency_ID, v_Currency_ID, 
				v_DateAcct, v_ConversionType_ID, r.AD_Client_ID, r.AD_Org_ID);
	    v_allocatedAmt := v_allocatedAmt + v_allocation;
	END LOOP;

	-- esto supone que las alocaciones son siempre no negativas; si esto no pasa, se van a retornar valores que no van a tener sentido
	v_AvailableAmt := ABS(v_Amt) - v_allocatedAmt;
	-- v_AvailableAmt aca DEBE ser NO Negativo si admeas, las suma de las alocaciones nunca superan el monto de la linea
	-- de cualquiera manera, por "seguridad", si el valor es negativo, se corrige a cero
    IF (v_AvailableAmt < 0) THEN
		RAISE NOTICE 'CashLine Available negative, correcting to zero - %',v_AvailableAmt ;
		v_AvailableAmt := 0.00;
    END IF;	
	--  el resultado debe ser 0 o de lo contrario tener el mismo signo que la linea; 
	IF (v_Amt < 0) THEN
		v_AvailableAmt := v_AvailableAmt * -1::numeric;
	END IF; 
	-- redondeo de moneda
	v_AvailableAmt :=  currencyRound(v_AvailableAmt,v_Currency_ID,NULL);
	RETURN	v_AvailableAmt;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cashlineavailable(integer, timestamp)
  OWNER TO libertya;

-- Función cashlineavailable(integer)
CREATE OR REPLACE FUNCTION cashlineavailable(p_c_cashline_id integer)
  RETURNS numeric AS
$BODY$ 
BEGIN
	RETURN cashlineavailable(p_c_cashline_id, null::timestamp);
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cashlineavailable(integer)
  OWNER TO libertya;