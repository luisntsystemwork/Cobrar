package org.libertya.ws.handler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.libertya.ws.bean.parameter.ParameterBean;
import org.libertya.ws.bean.result.ResultBean;
import org.libertya.ws.exception.ModelException;
import org.openXpertya.OpenXpertya;
import org.openXpertya.model.MUser;
import org.openXpertya.model.M_Column;
import org.openXpertya.model.M_Table;
import org.openXpertya.model.PO;
import org.openXpertya.util.DB;
import org.openXpertya.util.DisplayType;
import org.openXpertya.util.Env;
import org.openXpertya.util.Trx;

public abstract class GeneralHandler {

	/** Nombre de transaccion */
	protected String trxName = null;
	/** Nombre de usuario LY */
	public String userName = "";
	/** Nombres de parametros de la invocacion */
	protected String[] argNames = null; 
	/** Valores de parametros de la invocacion */
	protected Object[] argValues = null;
	
	/** Prefijos en log */
	public static final String ERROR_WS 				= "[WS_ERROR]    ";
	public static final String ERROR_MODEL 				= "[MODEL_ERROR] ";
	public static final String INFO_LOG	 				= "[INFO]        ";
	
	/** Archivo de log */
	public static final String LOG_FILENAME 			= "lyws.log";

	/** Sufijo para detalle de campos referenciados */
	public static final String REFERENCE_DETAIL 		= "__detail";
	
	/** Sufijo específico para campos referenciados con nombre Value */
	public static final String REFERENCE_DETAIL_VALUE	= "__value";

	/** Variables de entorno a recuperar */
	public static final String ENV_OXP_HOME 			= "OXP_HOME";
	public static final String ENV_OXP_WS_LOG 			= "OXP_WS_LOG";
	
	/** Separador entre nombre de tabla y nombre de columna para columnas referenciadas */
	public static final String REF_TABLE_COL_SEP		= ".";
	
	/** Cantidad de decimales a utilizar y metodo de redondeo */
	protected static final int BD_SCALE 				= 2;
	protected static final RoundingMode BD_ROUND_MODE 	= RoundingMode.HALF_EVEN;
	
	/** Especificando una columna con alguno de estos dos sufijos 
	 *  permite buscar la referencia por un criterio diferente a su ID
	 * 
	 * Ejemplos:
	 * 		bean.addColumnToHeader("M_Warehouse_ID__byValue", 	"Standard");
	 * 		bean.addColumnToHeader("CreatedBy__byName", 		"Supervisor"); 
	 */
	public static final String REF_SPECIFIED_BY_VALUE 	= "__byValue";
	public static final String REF_SPECIFIED_BY_NAME 	= "__byName";
	
	/**
	 * Realiza la configuración inicial a partir de la información recibida
	 * @param data parametros de configuración acceso
	 * @throws Exception en caso de error o rechazo
	 */
	protected void init(ParameterBean data, String[] argNames, Object[] argValues) throws Exception
	{
		// Argumentos
		this.argNames = argNames;
		this.argValues = argValues;
		
		// Guardar userName para uso en logger
		userName = data.getUserName();

// Comentado: 	Por algún motivo la gestión de contexto independiente por thread para LYWS colisiona con la de LYWeb, generando
//				un problema en esta última al utilizar ambas aplicaciones: las distintas sesiones en LYWeb se mezclan. Dado que
//				por el momento las operaciones LYWS son synchronized, quitar dicha gestión no presenta problema de ejecución en
//				lo que refiere a consistencia de datos.  TODO: determinar el motivo por el cual ocurre el mencionado conflicto.
// Update: 		A fin de aislar los contextos (LYWeb, LYWS, Server Tasks), se embeben en cada aplicación las librerías Libertya
//				que originalmente eran compartidas por todas la aplicaciones web, tal como OXP.jar y OXPSLib.jar. De esta forma
//				se aisla el contexto para cada aplicación, y por lo tanto sin la necesidad de gestión inpendiente del contexto. 
//      // Inicializar contexto para el thread actual (a fin de evitar problemas de concurrencia del contexto)
//		LYWSServerContext.newInstance();
//		Env.setContextProvider(new LYWSContextProvider());
		
		// Iniciar el entorno
		setClientOrg(data.getClientID(), data.getOrgID());
		startupEnvironment();
		
		// JDK1.6: [0] es getStackTrace(), [1] es init(), [2] es el método buscado
		saveToLogFile(INFO_LOG, "Ejecutando " + Thread.currentThread().getStackTrace()[2].getMethodName());
		
		// Validar login
		checkLogin(data.getUserName(), data.getPassword());
		
		// Setear valores adicionales
		setCurrency(data);
		setWarehouse(data);
	}
	
	/**
	 * 	Setea en el contexto las variables de compañía y organizacion
	 * @throws Exception en caso de indicar valores incorrectos
	 */
	protected void setClientOrg(Integer clientID, Integer orgID) throws Exception
	{
		// Setear clientID y orgID en el contexto
		if (clientID == null || clientID < 0)
			throw new Exception("Valor clientID (" + clientID + ") incorrecto. ");
		if (orgID == null || clientID < 0)
			throw new Exception("Valor orgID (" + orgID + ") incorrecto. ");
		Env.setContext(getCtx(), "#AD_Client_ID", clientID);
		Env.setContext(getCtx(), "#AD_Org_ID", orgID);
	}
	
	/**
	 * Setea la moneda de la compañía en el contexto
	 * @param data parametros recibidos como parametro
	 * @throws Exception en caso de error
	 */
	protected void setCurrency(ParameterBean data) throws Exception {
		// Incorporar al contexto la moneda de la compañía
		String sql = 	" SELECT C_Currency_ID " +
						" FROM C_AcctSchema a, AD_ClientInfo c " +
						" WHERE a.C_AcctSchema_ID=c.C_AcctSchema1_ID " +
						" AND c.AD_Client_ID = " + Env.getAD_Client_ID(getCtx());
		int currencyID = DB.getSQLValue(getTrxName(), sql);
		Env.setContext(getCtx(), "$C_Currency_ID", currencyID);
	}
	
	/**
	 * Setea en el contexto el almacen definido en la organizacion (o bien si 
	 * este no se encuentra definido toma un almacen perteneciente a la organización)
	 * @param data parametros recibidos como parametro
	 * @throws Exception en caso de error
	 */
	protected void setWarehouse(ParameterBean data) throws Exception 
	{
		// Recuperar el warehouseID a partir de la configuración de la organización
		if (data.getOrgID() > 0) {
			int warehouseID = DB.getSQLValue(getTrxName(), "SELECT M_Warehouse_ID FROM AD_OrgInfo WHERE AD_Org_ID = " + data.getOrgID());
			if (warehouseID > 0) {
				Env.setContext(getCtx(), "#M_Warehouse_ID", warehouseID);
				return;
			} 
			// En caso contrario recuperar algún warehouse de los existentes para la organización dada
			warehouseID = DB.getSQLValue(getTrxName(), 	" SELECT M_Warehouse_ID " +
														" FROM M_Warehouse " +
														" WHERE AD_Org_ID = " + data.getOrgID() +
														" AND isactive = 'Y'" +
														" ORDER BY created ASC LIMIT 1");
			if (warehouseID > 0) {
				Env.setContext(getCtx(), "#M_Warehouse_ID", warehouseID);
			}
		}
	}
	
	/**
	 * Configura en entorno inicial
	 * @throws Exception en caso de error
	 */
	protected void startupEnvironment() throws Exception
	{
		// Validar parametros recibidos
		if (getCtx() == null)
			throw new Exception ("Error al chequear login.  Contexto es null.");
		if (Env.getContext(getCtx(), "#AD_Client_ID") == null)
			throw new Exception ("Error al chequear login.  AD_Client_ID no esta configurada.");
		if (Env.getContext(getCtx(), "#AD_Org_ID") == null)
			throw new Exception ("Error al chequear login.  AD_Org_ID no esta configurada.");

		// Iniciar ambiente
		setOXPHome();
	  	if (!OpenXpertya.startup( false ))
	  		throw new Exception ("Error al iniciar entorno (Hay conexión a Base de Datos?) ");	
	}
	
	/** 
	 * Configura OXP_HOME y ubicacion de log segun las variables de entorno 
	 */
	protected void setOXPHome() throws Exception {
	  	// OXP_HOME seteada?
	  	String oxpHomeDir = System.getenv(ENV_OXP_HOME); 
	  	if (oxpHomeDir == null)
	  		throw new Exception ("La variable de entorno OXP_HOME no está seteada. ");
	  	// Cargar el entorno basico
	  	System.setProperty(ENV_OXP_HOME, oxpHomeDir);
	  	
	  	// OXP_WS_LOG seteada? Si no está seteada, utilizar la conf. de oxpHomeDir
	  	String oxpWSLog = System.getenv(ENV_OXP_WS_LOG);
	  	if (oxpWSLog == null)
	  		System.setProperty(ENV_OXP_WS_LOG, oxpHomeDir);
	  	else
	  		System.setProperty(ENV_OXP_WS_LOG, oxpWSLog);
	}
	
	/**
	 * 	Valida el acceso al WS
	 * @throws Exception en caso de error o acceso invalido
	 */
	protected void checkLogin(String userName, String password) throws Exception 
	{
		// Recuperar el usuario
		MUser user = MUser.get(getCtx(), userName, password);
		if (user==null)
			throw new Exception ("Error de acceso para usuario " + userName);
		// Setear valores correspondientes
		Env.setContext(getCtx(), "#AD_User_ID", user.getAD_User_ID());
		Env.setContext(getCtx(), "#AD_User_Name", user.getName());
		Env.setContext(getCtx(), "#AD_Language", "es_AR");	// FIXME: Desharcode, o ampliar ParameterBean y poner valor por defecto 
	}
	
	/**
	 * Recupera el contexto. En su primera invocacion genera 
	 * el contexto en funcion de los datos recibidos.
	 */
	protected Properties getCtx()
	{
		return Env.getCtx();
	}
	
	/**
	 * Recupera el nombre de la actual transacción en curso.
	 * En caso de no existir, la misma es generada en la invocacion
	 */
	protected String getTrxName()
	{
		if (trxName == null)
			trxName = Trx.createTrx(Trx.createTrxName()).getTrxName();
		return trxName;
	}
	
	/**
	 * Sobrecarga de metodo: setValue simple, sin force de campos, ni seteo de valores por defecto.
	 */
	protected void setValues(PO po, HashMap<String, String> map, boolean newRecord) throws Exception {
		setValues(po, map, newRecord, false, false); 
	}
	
	/**
	 * Setea cada uno de los miembros del PO bajo el siguiente criterio:
	 * 		Recorre las columnas pertenecientes al PO y busca en colValues los datos
	 *  	que deben ser cargados en cada uno de estos.  De esta manera, solo se
	 *  	cargarán las columnas del PO que esten como datos de entrada en colValues.
	 * @param po 
	 * 			objeto a setearle los valores
	 * @param colName 
	 * 			nombre de las columnas a utilizar como datos de entrada
	 * @param colValues 
	 * 			valores a cargar en cada columna
	 * @param newRecord 
	 * 			indica si se está seteando valores de un nuevo registro o de uno existente
	 * @param force 
	 * 			si está en true utiliza set_ValueNoCheck 
	 * @param setDefaultValues 
	 * 			si está en true, seteará valores por defecto en las columnas donde no haya cargado un dato en la map
	 */
	protected void setValues(PO po, HashMap<String, String> map, boolean newRecord, boolean force, boolean setDefaultValues) throws Exception
	{
		// recuperar las columnas de la tabla a rellenar
		M_Table aTable = M_Table.get(getCtx(), po.get_TableName());
		M_Column[] columns = aTable.getColumns(false);

		// Pasar a minuscula las keys a fin de normalizar el criterio de busqueda
		map = toLowerCaseKeys(map);
		
		// recorrer las columnas y cargar los datos con el array de valores recibido
		for (M_Column aColumn : columns)
		{
			// Existe el valor en la map?
			boolean containsKey = false;
			String value = null;
			
			// Intentar setear el valor para la columna (si está dentro de los recibidos como parametro)
			if (map.keySet().contains(aColumn.getColumnName().toLowerCase()))
			{
				// Castear el string recibido al tipo que corresponda, y determinar si fue posible o no mediante ok
				// El valor del parametro puede ser null, de tipo String, Integer, BigDecimal, Timestamp o byte[]
				containsKey = true;
				value = map.get(aColumn.getColumnName().toLowerCase());
			}
			// Intentar setear el valor para la columna referencial, especificado por su value (si está dentro de los recibidos como parametro)
			else if (map.keySet().contains(aColumn.getColumnName().toLowerCase() + REF_SPECIFIED_BY_VALUE.toLowerCase())) 
			{
				containsKey = true;
				value = retrieveIDByField(aColumn, "value", map.get(aColumn.getColumnName().toLowerCase() + REF_SPECIFIED_BY_VALUE.toLowerCase()));
			}
			// Intentar setear el valor para la columna referencial, especificado por su name (si está dentro de los recibidos como parametro)
			else if (map.keySet().contains(aColumn.getColumnName().toLowerCase() + REF_SPECIFIED_BY_NAME.toLowerCase())) 
			{
				containsKey = true;
				value = retrieveIDByField(aColumn, "name", map.get(aColumn.getColumnName().toLowerCase() + REF_SPECIFIED_BY_NAME.toLowerCase()));
			}
			
			// Si hay un valor especificado, entonces setearlo
			if (containsKey)
			{
				// Si el valor alojado en la columna es el mismo que el ya existente, omitir el set_Value; 
				if (po.get_ValueAsString(aColumn.getColumnName()).equals(value))
					continue;
				
				// Si se recibe force en true, o bien si es un nuevo registro y la columna no esta marcada como actualizable, entonces forzar el valor
				// En caso de error, devolver dicho mensaje
				if (!setValue(po, aColumn, value, newRecord, force))
					throw new ModelException("No es posible setear valor " + value + " en columna " + aColumn.getColumnName() + " de entidad " + po.get_TableName() + " (es actualizable?)");
			}
			// Si no se ha recibido dentro de los parámetros un valor, verificar si hay algún valor cargado como predefinido			
			else 
			{
				// Setear valor por defecto unicamente si expresamente se desea utilizar está funcionalidad
				if (setDefaultValues)
					setDefaultValue(aColumn, po, newRecord);
			}
		}				
	}
	
	/**
	 * Setea el valor por defecto cargado en metadatos para la columna dada.
	 * 		Unicamente para tipos de dato Entero y BigDecimal, si éstos NO contienen valores de tipo @expresion@
	 * @param aColumn columna a setear valor
	 * @param po objeto a asignar valor para la columna dada
	 * @param newRecord flag que indica si estamos insertando o actualizacion
	 * @throws ModelException en caso de error
	 */
	protected void setDefaultValue(M_Column aColumn, PO po, boolean newRecord) throws ModelException
	{
		Class<?> clazz = DisplayType.getClass(aColumn.getAD_Reference_ID(), false);
		String columnName = aColumn.getColumnName();
		try {
			// Solo para tipo Entero
			if (Integer.class == clazz &&
					(po.get_Value(columnName) == null || "0".equals(po.get_ValueAsString(columnName))) &&
					(aColumn.getDefaultValue() != null && aColumn.getDefaultValue().length() > 0 && !aColumn.getDefaultValue().startsWith("@")) )
				setValue(po, aColumn, aColumn.getDefaultValue(), newRecord, true);
			
			// Solo para tipo BigDecimal		
			if (BigDecimal.class == clazz &&
					(po.get_Value(columnName) == null || BigDecimal.ZERO.compareTo(new BigDecimal(po.get_ValueAsString(columnName))) == 0) &&
					(aColumn.getDefaultValue() != null && aColumn.getDefaultValue().length() > 0 && !aColumn.getDefaultValue().startsWith("@")) )
				setValue(po, aColumn, aColumn.getDefaultValue(), newRecord, true);
		}
		catch (Exception e) {
			throw new ModelException("No es posible setear valor por defecto" + aColumn.getDefaultValue() + " en columna " + aColumn.getColumnName() + " de entidad " + po.get_TableName() + "(" + e.getMessage() + ")");			
		}
/*
 * DADA LAS COMPLEJIDAD PRESENTADA, POR EL MOMENTO COMENTADA LA LOGICA DE DEFAULTS COMPLETA
 * """"""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
 * Básicamente debido a que en los defaultValue de AD_Column se presentan expresiones 
 * del tipo @IsSOTrx@, @M_Shipper_ID@, @sql=....@ las cuales dependen de un contexto 
 * mayor del que se cuenta en los Web Services. Adicionalmente, la clase encargada de 
 * parsear y generar el valor definitivo es MField, la cual debe ser instanciada 
 * mediante un MFieldVO, lo cual nos arrima considerablemente a temas relacionados 
 * con ventanas (requiere AD_Window_ID, AD_Tab_ID, etc).
 */		
//		if (newRecord && po.get_Value(aColumn.getColumnName()) == null && aColumn.getDefaultValue() != null && aColumn.getDefaultValue().length() > 0) 
//		{
//			try {
//				// Recuperar algun field relacionado con la columna
//				PreparedStatement stmt = DB.prepareStatement(" SELECT * FROM AD_Field_v WHERE AD_Column_ID = " + aColumn.getAD_Column_ID() + " LIMIT 1", getTrxName());
//				ResultSet rs = stmt.executeQuery();
//				if (!rs.next())
//					return;
//
//				// Generar el field y determinar el default value
//				MFieldVO fieldvo = MFieldVO.create(getCtx(), -1, -1, -1, false, rs);
//				MField field = new MField(fieldvo);
//				if (field != null && field.getDefault() != null && !setValue(po, aColumn, field.getDefault().toString(), true, newRecord))
//					throw new Exception("-");
//				
//				// Cerrar resultset y statement 
//				rs.close();
//				rs = null;
//				stmt.close();
//				stmt = null;
//			}
//			catch (Exception e) {
//				throw new ModelException("No es posible setear valor por defecto" + aColumn.getDefaultValue() + " en columna " + aColumn.getColumnName() + " de entidad " + po.get_TableName() + "(" + e.getMessage() + ")");						
//			}
//		}
	}
	
	
	/**
	 * Setea un valor en una columna de un PO dado.
	 * @param po el objeto persistente
	 * @param aColumn la columna a setear
	 * @param value el valor a setear
	 * @param force fuerza usando set_ValueNoCheck en lugar de set_Value tradicional
	 * @param newRecord indica si se está seteando valores de un nuevo registro o de uno existente
	 * @return true si fue posible setear el valor, o false en caso contrario
	 */
	protected boolean setValue(PO po, M_Column aColumn, String value, boolean newRecord, boolean force) throws ModelException {
		boolean ok = false;
		try {
			// Si force = true, o bien se esta insertando un nuevo registro, omitir validaciones (usando set_ValueNoCheck)
			if (force || (newRecord && !aColumn.isUpdateable()))
				ok = 
					(null == value && po.set_ValueNoCheck(aColumn.getColumnName(), value)) ||
					(String.class == DisplayType.getClass(aColumn.getAD_Reference_ID(), false) && po.set_ValueNoCheck(aColumn.getColumnName(), value)) ||
					(Integer.class == DisplayType.getClass(aColumn.getAD_Reference_ID(), false) && po.set_ValueNoCheck(aColumn.getColumnName(), Integer.parseInt(value))) ||
					(BigDecimal.class == DisplayType.getClass(aColumn.getAD_Reference_ID(), false) && po.set_ValueNoCheck(aColumn.getColumnName(), new BigDecimal(value))) ||
					(Timestamp.class == DisplayType.getClass(aColumn.getAD_Reference_ID(), false) && po.set_ValueNoCheck(aColumn.getColumnName(), Timestamp.valueOf(value))) ||
					(byte[].class == DisplayType.getClass(aColumn.getAD_Reference_ID(), false) && po.set_ValueNoCheck(aColumn.getColumnName(), value.getBytes()));
			else {
				// Verificar por isAlwaysUpdateable
				checkAlwaysUpdatable(po, aColumn);
				ok =  
					(null == value && po.set_Value(aColumn.getColumnName(), value)) ||
					(String.class == DisplayType.getClass(aColumn.getAD_Reference_ID(), false) && po.set_Value(aColumn.getColumnName(), value)) ||
					(Integer.class == DisplayType.getClass(aColumn.getAD_Reference_ID(), false) && po.set_Value(aColumn.getColumnName(), Integer.parseInt(value))) ||
					(BigDecimal.class == DisplayType.getClass(aColumn.getAD_Reference_ID(), false) && po.set_Value(aColumn.getColumnName(), new BigDecimal(value))) ||
					(Timestamp.class == DisplayType.getClass(aColumn.getAD_Reference_ID(), false) && po.set_Value(aColumn.getColumnName(), Timestamp.valueOf(value))) ||
					(byte[].class == DisplayType.getClass(aColumn.getAD_Reference_ID(), false) && po.set_Value(aColumn.getColumnName(), value.getBytes()));
			}
			return ok;
			}
		catch (Exception e) {
			throw new ModelException("Error al setear valor " + (value==null?"null":value) + " en columna " + aColumn.getColumnName() + " de entidad " + po.get_TableName() + ". " + e);
		}
	}
	
	/**
	 * Logica de validación por "siempre actualizable" para columnas incluidas en tablas con lógica de documentos
	 * @param po el objeto a intentar modificar
	 * @param aColumn columna a intentar setear un nuevo valor.
	 * @throws ModelException en caso de que el po se encuentra procesado y la columna no tiene setead isAlwaysUpdateable en true 
	 * 
	 * La logica NO generará excepciones si el documento puede ser actualizable, debido a alguna de las siguientes razones: 
	 * 		1) No existe el dato processed de po, con lo cual el valor isAlwaysUpdateable ya no es necesario
	 * 		2) El dato processed de po se encuentra en false, con lo cual estamos en un escenario similar a 1
	 * 		3) El dato processed de po se encuentra en true, pero la columna está configurada como isAlwaysUpdateable
	 *   
	 */
	protected void checkAlwaysUpdatable(PO po, M_Column aColumn) throws ModelException {
		// Intentar recuperar el campo Processed
		Object oo = po.get_Value("Processed");
		// Si no hay campo processed, entonces solo hay que evaluar la logica de isUpdateable
		if (oo == null)
			return;
		// Determinar si el registro está procesado o no (similar al codigo generado en las clases X_
		boolean isProcessed = (oo instanceof Boolean) ? ((Boolean)oo).booleanValue() : "Y".equals(oo);
		// Si no el registro no fue procesado, entonces devolver true
		if (!isProcessed)
			return;
		// Si llegamos hasta aqui es debido a que solo se podrá actualizar si el campo isAlwaysUpdateable = Y
		if (!aColumn.isAlwaysUpdateable())
			throw new ModelException(" El documento se encuentra procesado, y la columna a setear no esta configurada como Siempre Actualizable.");
	}
	
	/**
	 * Retorna el ID de un registro a partir de su value, name, etc.
	 * @param aColumn columna que referencia a otro registro
	 * @param fieldName campo por el cual determinar el registro a buscar su ID
	 * @param fieldValue valor del campo por el cual determinar el registro a buscar su ID
	 * @return el ID del registro o -1 en caso contrario
	 */
	protected String retrieveIDByField(M_Column aColumn, String fieldName, String fieldValue) 
	{
		// Valor por defecto
		final String DEFAULT_RETVALUE = "-1";
		
		// Sin la info en cuestión, imposible continuar
		if (aColumn==null || fieldName==null || fieldValue==null || fieldName.length()==0 || fieldValue.length()==0)
			return DEFAULT_RETVALUE;
		
		try {
			// Recuperar nombre de tabla y columna referenciada
			ArrayList<String> reference = getReferencedColumnAndTableName(aColumn);
			String tableName = reference.get(0);
			String columnName = reference.get(1);

			// si se obtuvo una referencia válida, recuperar el dato
			if (columnName!=null && tableName!=null && columnName.length()>0 && tableName.length() > 0) {
				Integer recordID = DB.getSQLValue(getTrxName(), "SELECT " + columnName + " FROM " + tableName + " WHERE " + fieldName + " = '" + fieldValue + "'" + " AND AD_Client_ID IN (0, " + Env.getAD_Client_ID(getCtx()) + ")");
				return recordID.toString();
			}
		}
		catch (Exception e) {
			System.err.println("Error en retrieveIDByField: " + e.toString());
			return DEFAULT_RETVALUE;	
		}
		// -1 si no hay nada que hacer
		return DEFAULT_RETVALUE;
	}
	
	
	/**
	 * Pasa a minuscula todas las keys de una map
	 * @param map la map a convertir
	 * @return la misma map, pero con las keys pasadas a minuscula
	 */
	protected HashMap<String, String> toLowerCaseKeys(HashMap<String, String> map)
	{
		HashMap<String, String> result = new HashMap<String, String>();
		for (String key : map.keySet())
			result.put(key.toLowerCase(), map.get(key));
		return result;
	}
	
	/**
	 * Pasa a minuscula todas las entradas de una colección
	 * @param value la coleccion a convertir
	 * @return la misma coleccion pero con sus entradas pasadas a minuscula
	 */
	protected ArrayList<String> toLowerCaseValues(ArrayList<String> values) 
	{
		ArrayList<String> result = new ArrayList<String>();
		for (String value : values)
			result.add(value.toLowerCase());
		return result;
	}
	
	
	/**
	 * Dada una serie de keys contenida en una map, validar si alguna corresponde a la columna indicada
	 * 	Se utiliza esta busqueda debido a que map.keySet().contains(columnName) es case sensitive. 
	 * @param map mapa donde buscar
	 * @param columnName nombre de columna a buscar
	 * @return true si se encuentra o false si no se encuentra.
	 */
	protected boolean containsColumn(HashMap<String, String> map, String columnName)
	{
		for (String key : map.keySet())
			if (key.equalsIgnoreCase(columnName))
				return true;
		return false;
	}
	
	/**
	 * Vuelca a una nueva coleccion de tipo map el conjunto de valores contenido en un PO.
	 * 		Sobrecarga de método poToMap(po, includeNamedReferences, baseMap, additionalPrefix) por compatibilidad
	 * @return la coleccion resultante.
	 */
	protected HashMap<String, String> poToMap(PO po, boolean includeNamedReferences, String[] referencedTablesColumns) {
		return poToMap(po, includeNamedReferences, null, "", null, referencedTablesColumns);
	}

	/**
	 * Sobrecarga por compatibilidad
	 */
	protected HashMap<String, String> poToMap(PO po, boolean includeNamedReferences) {
		return poToMap(po, includeNamedReferences, null, "", null, null);
	}
	
	/**
	 * Sobrecarga por compatibilidad
	 */
	protected HashMap<String, String> poToMap(PO po, boolean includeNamedReferences, HashMap<String, String> baseMap, String additionalPrefix, ArrayList<String> filterColumns) {
		return poToMap(po, includeNamedReferences, baseMap, additionalPrefix, filterColumns, null);
	}
	
	/**
	 * Vuelca a una (nueva o existente) coleccion de tipo map el conjunto de valores contenido en un PO
	 * @param po el PO a volcar
	 * @param includeNamedReferences si se desea incluir, además de los _IDs a otras tablas, el nombre/value/etc de la tabla destino 
	 * 			Para el dato en cuestión recupera la primer columna no nula de las de tipo identificador.			
	 * 			Este dato es incluido en la map con el mismo nombre del campo, pero con subfijo REFERENCE_DETAIL. (ej: nombreDelCampo__detail). 
	 * @param baseMap En caso de recibir una map, los campos obtenidos son adicionados a la baseMap recibida.
	 *				  Si se recibe null, se crea una nueva map en donde cargar los datos a devolver.   
	 * @param additionalPrefix prefijo adicional que se incorpora inmediatamente antes del nombre de la columna (ej: prefijoNombreDeColumna)
	 * @param filterColumns solo incluye las columnas indicadas en este parametro como resultado.  Si se recibe null o sin entradas, entonces devolver todas
	 * @param referencedTablesColumns mostrar columnas especificas de los registros referenciados.  Ejemplo: CreatedBy.Name, C_BPartner_ID.Description, etc. <br>
	 * 			Se debe especificar de la siguiente manera: ColumnaDeReferenciaEnTablaOrigen.ColumnaARecuperarEnTablaDestino <br>
	 * 			Ejemplo con documentQueryInvoices: <br>
	 * 				Obtener el nombre del usuario que generó la factura (C_Invoice.CreatedBy -> AD_User.Name), indicar "CreatedBy.Name" <br>
	 * 				Obtener la descripción de la EC de la factura (C_Invoice.C_BPartner_ID -> C_BPartner.Description), indicar "C_BPartner_ID.Description" <br>
	 * @return la colección resultante
	 */
	protected HashMap<String, String> poToMap(PO po, boolean includeNamedReferences, HashMap<String, String> baseMap, String additionalPrefix, ArrayList<String> filterColumns, String[] referencedTablesColumns)
	{
		HashMap<String, String> map = (baseMap == null ? new HashMap<String, String>() : baseMap);
		if (additionalPrefix==null)
			additionalPrefix = "";
		
		// recuperar las columnas de la tabla a rellenar
		M_Table aTable = M_Table.get(getCtx(), po.get_TableName());
		M_Column[] columns = aTable.getColumns(false);
		
		// recorrer las columnas y cargar los datos con el array de valores recibido
		for (M_Column aColumn : columns)
		{
			// Verificar si la columna actual debe ser devuelta como parte del resultado
			if (filterColumns!=null && filterColumns.size()>0 && !toLowerCaseValues(filterColumns).contains(aColumn.getColumnName().toLowerCase()))
					continue;
			
			// Cargar dato a la map
			map.put(additionalPrefix + aColumn.getColumnName(), po.get_ValueAsString(aColumn.getColumnName()));
			
			// Referencia de name/value en tablas referenciadas.  Recuperación de columnas referenciadas segun referencedTablesColumns
			if ((includeNamedReferences || (referencedTablesColumns!=null && referencedTablesColumns.length>0)) && po.get_Value(aColumn.getColumnName())!=null && isTableReference(aColumn))
			{
				try
				{
					// Recuperar nombre de tabla y columna referenciada
					ArrayList<String> reference = getReferencedColumnAndTableName(aColumn);
					String tableName = reference.get(0);
					String columnName = reference.get(1);					
					// si se obtuvo una referencia válida, recuperar el dato
					if (columnName!=null && tableName!=null && columnName.length()>0 && tableName.length() > 0)
					{
						// Obtener las columnas identificadoras de la tabla destino
						StringBuffer identifierColumns = new StringBuffer("");
						for (String identifierColumn : M_Table.getIdentifierColumns(getTrxName(), tableName))
							identifierColumns.append(" COALESCE(").append(identifierColumn).append("::varchar, '') || '_' || ");
						// Adicionalmente, existe en la tabla referenciada la columna value?
						int valueCol = DB.getSQLValue(getTrxName(), "SELECT count(1) " +
																	" FROM information_schema.columns " +
																	" WHERE table_name = '"+tableName.toLowerCase()+"'" +
																	" AND column_name = 'value'");
						// Si no hay identificadores, no hay columna Value, y no hay columnas adicionales a recuperar... entonces no hay nada mas que hacer
						if (identifierColumns.length()==0 && valueCol==0 && (referencedTablesColumns==null || referencedTablesColumns.length==0))
							continue;
						// Si hay identificadores, borrar ultimo concatenador
						if (identifierColumns.length()>0)
							identifierColumns.delete(identifierColumns.length()-11, identifierColumns.length()-1);
						
						// Obtener el dato referenciado y cargar los identificadoes en la map de detalles (y el value también si es que este existe)
						// Incorporar también las columnas que se requieran segun especificacion en referencedTablesColumns
						String sql = " SELECT null as dummyColumnForResultSet " +
										(identifierColumns.length() > 0 ? ", COALESCE(" + identifierColumns.toString() + ") as detail " : "") + 
										(valueCol>0?", value ":"") +
										addReferencedTablesColumnsToQuery(referencedTablesColumns, aColumn) +
										" FROM " + tableName + 
										" WHERE " + columnName + " = ? ";
						PreparedStatement ps = DB.prepareStatement(sql, getTrxName());
						Object value;
						Integer intValue;
						String strValue = String.valueOf(po.get_Value(aColumn.getColumnName()));
						try{
							intValue = Integer.parseInt(strValue);
							ps.setInt(1, intValue);
						} catch(NumberFormatException cce){
							value = strValue;
							ps.setObject(1, value);
						}
						ResultSet rs = ps.executeQuery();
						if(rs.next()) {
							// Cargar la nomina de identificadores del registro referenciado
							if (identifierColumns.length() > 0 && rs.getString("detail") != null)
									map.put(additionalPrefix + aColumn.getColumnName() + REFERENCE_DETAIL, rs.getString("detail"));
							// Cargar el value del registro referenciado
							if (valueCol > 0 && rs.getString("value") != null)
								map.put(additionalPrefix + aColumn.getColumnName() + REFERENCE_DETAIL_VALUE, rs.getString("value"));
							// Cargar las columnas adicionales requeridas segun referencedTablesColumns
							addReferencedTablesColumnsToMap(referencedTablesColumns, aColumn, map, rs, additionalPrefix);
						}
								
					}
				}
				catch (Exception e) {
					System.out.println(" mapToPo, excepcion en columna: " + aColumn.getColumnName());
					e.printStackTrace();
				}				
			}
		}
		
		return map;
	}
	
	/**
	 * Amplia el query de recuperación de columnas referenciadas segun lo recibido en referencedTablesColumns
	 * @param referencedTablesColumns nomina de columnas referenciadas, por ejemplo CreatedBy.Name
	 * @param aColumn columna que se está evaluando en este momento
	 * @return la ampliacion de la query con el conjunto de datos a recuperar 
	 */
	protected String addReferencedTablesColumnsToQuery(String[] referencedTablesColumns, M_Column aColumn) {
		StringBuffer retValue = new StringBuffer();
		if (referencedTablesColumns==null || referencedTablesColumns.length==0)
			return retValue.toString();
		// Iterar por las referencias, considerando unicamente las de la tabla 
		for (String aReferencedColumn : referencedTablesColumns) {
			// Tiene aColumn (por ejemplo CreatedBy) coincidencia con aReferencedColumn (por ejemplo CreatedBy.Description)?
			// De ser asi, indicar que se quiere recuperar la información correspondiente (en el ejemplo columna Description)
			String refColSource = aReferencedColumn.substring(0, aReferencedColumn.indexOf(REF_TABLE_COL_SEP));
			String refColTarget = aReferencedColumn.substring(aReferencedColumn.indexOf(REF_TABLE_COL_SEP)+1);
			if (aColumn.getColumnName().equalsIgnoreCase(refColSource))
				retValue.append(", ").append(refColTarget);
		}
		return retValue.toString();
	}
	
	/**
	 * Incopora a la map un dato recuperado a partir del pedido de obtencion de columnas en referencedTablesColumns 
	 * @param referencedTablesColumns nomina total de columnas a recuperar
	 * @param aColumn columna que se está procesando en este momento
	 * @param map valores a retornar
	 * @param rs informacion recuperada
	 * @param additionalPrefix prefijo adicional
	 * @throws Exception
	 */
	protected void addReferencedTablesColumnsToMap(String[] referencedTablesColumns, M_Column aColumn, HashMap<String, String> map, ResultSet rs, String additionalPrefix) throws Exception {
		if (referencedTablesColumns==null || referencedTablesColumns.length==0)
			return;
		for (String aReferencedColumn : referencedTablesColumns) {
			// Tiene aColumn (por ejemplo CreatedBy) coincidencia con aReferencedColumn (por ejemplo CreatedBy.Description)?
			// De ser asi, recuperar el dato desde el resultSet (en el ejemplo columna Description) y volcarlo en la map
			String refColSource = aReferencedColumn.substring(0, aReferencedColumn.indexOf(REF_TABLE_COL_SEP));
			String refColTarget = aReferencedColumn.substring(aReferencedColumn.indexOf(REF_TABLE_COL_SEP)+1);
			if (aColumn.getColumnName().equalsIgnoreCase(refColSource) && rs.getString(refColTarget) != null)
				map.put(additionalPrefix + aReferencedColumn, rs.getString(refColTarget));
		}
		
	}
	
	
	/**
	 * Dada una M_Columnm, retorna el nombre de la tabla y columna de tipo ID referenciada en dicha M_Column
	 * @param aColumn columna que contiene la referencia
	 * @return un ArrayList conteniendo:
	 * 		1. Nombre de la tabla referenciada
	 * 		2. Nombre de la columna de tipo ID referenciada
	 */
	protected ArrayList<String> getReferencedColumnAndTableName(M_Column aColumn) throws Exception {
		String tableName = null;
		String columnName = null;
		// Si la columna termina en _ID la tabla referenciada se determina de manera directa
		if(aColumn.getColumnName().toUpperCase().endsWith("_ID"))
		{
			tableName = aColumn.getColumnName().substring(0, aColumn.getColumnName().lastIndexOf("_"));
			columnName = aColumn.getColumnName();
		}
		// Si la columna tiene tiene una referencia seteada, entonces buscar la definición allí
		if(aColumn.getAD_Reference_Value_ID() != 0)
		{
			// Recuperar nombre de tabla y columna que está referenciando la columna actual  
			int tableID = 0, key = 0;
			tableID = getTableIDFromReferenceID(aColumn.getAD_Reference_Value_ID(), getTrxName());
			key = getKeyFromReferenceID(aColumn.getAD_Reference_Value_ID(), getTrxName());
			tableName = getTableNameFromTableID(tableID, getTrxName());
			columnName = M_Column.getColumnName(Env.getCtx(), key);
		}
		ArrayList<String> retValue = new ArrayList<String>();
		retValue.add(tableName);
		retValue.add(columnName);
		return retValue;
	}
	
	/**
	 * Verifica si una columna dada referencia a 
	 * @param aColumn la columna a evaluar
	 * @return verdadero si hay referencia hacia otra tabla o falso en caso contrario 
	 */
	protected boolean isTableReference(M_Column aColumn)
	{
		boolean isReference = false;
		if(DisplayType.isTableReference(aColumn.getAD_Reference_ID()))
			isReference = true;
		else if(!aColumn.isKey() && aColumn.getColumnName().toUpperCase().endsWith("_ID")){
			String tablename = aColumn.getColumnName().substring(0, aColumn.getColumnName().lastIndexOf("_"));
			String sql = "SELECT count(1) FROM ad_table WHERE upper(tablename) = upper(?)";
			isReference = DB.getSQLValue(trxName, sql, tablename) > 0;
		}
		return isReference;
	}
	

	
	/**
	 * Procesa un error, rollbackeando la transaccion, 
	 * generando el log correspondiente y retornando el valor en cuestion
	 * @param e exception a procesar
	 * @param result el tipo de ResultBean a utilizar (puede ser alguna subclase)
	 * @param wsParameters parametros usados en la invocacion al WS
	 * @return resultbean el error cargado
	 */
	protected ResultBean processException(Exception e, ResultBean result, String wsParameters) {
		/* Rollback transaccion */ 
		Trx.getTrx(getTrxName()).rollback();
		
		/* Armar mensaje de error */
		StringBuffer errValue = new StringBuffer("");
		errValue.append(e.toString()).append(" (");
		for (StackTraceElement elem : e.getStackTrace())
			errValue.append(elem.toString()).append(", ");
		errValue.replace(errValue.length()-2, errValue.length()-1, ")");
		errValue.append("\n - Parameters - \n ").append(wsParameters);
		
		/* Enviar al log */
		saveToLogFile(e instanceof ModelException ? ERROR_MODEL : ERROR_WS, errValue.toString());
		
		/* === Retornar error === */
		result.setError(true);
		result.setErrorMsg(errValue.toString());
		return result;
	}

	/**
	 * Sobrecarga en la que no es necesario especificar el tipo de Result, utiliza ResultBean
	 */
	protected ResultBean processException(Exception e, String wsParameters) {
		return processException(e, new ResultBean(), wsParameters);
	}
	
	/**
	 * Cierra la transaccion actual
	 */
	protected void closeTransaction() {
		Trx.getTrx(getTrxName()).close();
	}

	/**
	 * Commit de la transaccion actual
	 */
	protected void commitTransaction() {
		Trx.getTrx(getTrxName()).commit();
	}

	/**
	 * Escribe data al archivo de log del ws
	 * @param data informacion a incorporar
	 */
	protected boolean saveToLogFile(String level, String data)
	{
		try
		{
			File file = new File(System.getProperty(ENV_OXP_WS_LOG) + File.separator + LOG_FILENAME);
			if(!file.exists()) 	{
				System.out.println("Creando archivo de log en: " + file.getPath() );
				file.createNewFile();
			}
			FileWriter fileWritter = new FileWriter(file.getPath(), true);
			BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
		    bufferWritter.write(Env.getDateTime("yyyy-MM-dd HH:mm:ss ") +
		    					level + 
		    					" (" + userName + ") - " +
								this.getClass().getName() + " - " +
		    					data +  
		    					"\n");
		    bufferWritter.flush();
		    bufferWritter.close();
		    return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Copia los valores de un PO origen a uno destino,
	 * matcheando por el nombre de la columna unicamente.
	 * La copia se realiza SOLO si el destino no tiene un dato cargado
	 * Omite columnas especiales, de components y replicacion
	 * @param source PO origen
	 * @param target PO destino
	 */
	protected void copyPOValues(PO source, PO target)
	{
		// recuperar las columnas de la tabla destino
		M_Table targetTable = M_Table.get(getCtx(), target.get_TableName());
		M_Column[] targetColumns = targetTable.getColumns(false);
		
		// Recorrer las columnas destino y verificar si hay que copiar un dato
		for (M_Column targetColumn : targetColumns)
		{
			// Nombre de columna destino
			String columnName = targetColumn.getColumnName();
			
			// Si el destino ya tiene un dato, se omite
			if (target.get_Value(columnName)!=null && target.get_ValueAsString(columnName).length()>0)
				continue;
			
			// Columnas especiales se omiten
			if (columnName.startsWith("Created") ||
				columnName.startsWith("Updated") ||
				columnName.equals("AD_Client_ID") ||
				columnName.equals("AD_Org_ID") ||
				columnName.equals("AD_ComponentVersion_ID") ||
				columnName.equals("AD_ComponentObjectUID") ||
				columnName.equalsIgnoreCase("retrieveUID") ||
				columnName.equalsIgnoreCase("repArray") ||
				columnName.equalsIgnoreCase("dateLastSentJMS") ||
				targetColumn.isKey())
				continue;
			
			// Si el origen tiene un dato, entonces setear el destino
			if (source.get_Value(columnName)!=null && source.get_ValueAsString(columnName).length()>0)
				target.set_Value(columnName, source.get_Value(columnName));
		}
	}
	
	/**
	 * Recupera un array de PO a partir de su ID (en este caso será de longitud 1) o bien indicando una columna y el valor que ésta debe tener
	 * @param poID ID del PO (primera opcion de búsqueda)
	 * @param columnName nombre de la columna por la cual buscar (segunda opción de búsqueda, primer parametro) 
	 * @param columnValue criterio de busqueda sobre dicha columna (segunda opción de búsqueda, segundo parametro)
	 * @param filterByOrg para la segunda opcion de busqueda, adiciona un filtrado por organizacion (si AD_Org_ID de login != 0)
	 * @param filterByClient para la segunda opcion de busqueda, adiciona un filtrado por compañía (si AD_Client_ID de login != 0)  
	 * @param oneResultOnly requiere que solo se obtenga un resultado
	 * @param noResultThrowsException especifica si no se obtienen resultados: disparar una excepcion o no
	 * @throws ModelException en caso de que la búsqueda devuelva más de un resultado (si oneResultOnly es true)
	 * @throws ModelException en caso de que la búsqueda no devuelva resultados (si noResultThrowsExeption es true)
	 * @return el PO ya instanciado o null en caso de no encontrarlo (si noResultThrowsExeption es false)
	 */
	public PO[] getPOs(String tableName, int poID, String columnName, String columnValue, boolean filterByOrg, boolean filterByClient, boolean oneResultOnly, boolean noResultThrowsException) throws ModelException
	{
		PO[] pos = null;
		
		// Recuperar la tabla indicada
		M_Table table = M_Table.get(getCtx(), tableName);

		PO aPO = null;
		// Se busca por su ID?
		if (poID > 0) {
			aPO = table.getPO(poID, getTrxName());
			pos = new PO[1];
			pos[0] = aPO;
		}
		// Se busca por un par columnName/ColumnValue?
		else if (columnName!=null && columnValue!=null && columnName.length()>0 && columnValue.length()>0)
		{
			int[] ids = null;
			String filterQuery = columnName + " = '" + columnValue + "'";
			// Si filterByOrg es true && orgID>0 => Si hay un org especificado en el login, se filtra por org.  
			// Si filterByClient es true && clientID>0 => Si hay un client especificada en el login, se filtra por client.  
			if (filterByOrg && Env.getAD_Org_ID(getCtx())>0)
				filterQuery += " AND AD_Org_ID = " + Env.getAD_Org_ID(getCtx());
			if (filterByClient && Env.getAD_Client_ID(getCtx())>0)
				filterQuery += " AND AD_Client_ID = " + Env.getAD_Client_ID(getCtx());
			ids = PO.getAllIDs(tableName, filterQuery, getTrxName());
			// Se obtuvieron varios resultados?
			if (ids!=null && ids.length>1 && oneResultOnly)
				throw new ModelException("El criterio de busqueda retornó más de un resultado en tabla " + tableName + " con los criterios especificados");
			// Procesar el resultado (si es que el mismo existe)
			if (ids!=null && ids.length>0) {
				int i=0;
				pos = new PO[ids.length];
				for (int id : ids) {
					aPO = table.getPO(id, getTrxName());
					pos[i++] = aPO;
				}
			}
		}
		// No se pudo obtener un objeto?
		if ((pos == null || pos.length == 0 || pos[0].getID() == 0) && noResultThrowsException)
			throw new ModelException("No se pudo recuperar un registro para la tabla " + tableName + " con los criterios especificados.");
		// Retornar el objeto instanciado
		return pos;
	}
	
	/**
	 * Recupera un PO a partir de su ID o bien indicando una columna y el valor que ésta debe tener
	 * @param poID ID del PO (primera opcion de búsqueda)
	 * @param columnName nombre de la columna por la cual buscar (segunda opción de búsqueda, primer parametro) 
	 * @param columnValue criterio de busqueda sobre dicha columna (segunda opción de búsqueda, segundo parametro)
	 * @param filterByOrg para la segunda opcion de busqueda, adiciona un filtrado por organizacion (si AD_Org_ID de login != 0)
	 * @param filterByClient para la segunda opcion de busqueda, adiciona un filtrado por compañía (si AD_Client_ID de login != 0)  
	 * @param oneResultOnly requiere que solo se obtenga un resultado
	 * @param noResultThrowsException especifica si no se obtienen resultados: disparar una excepcion o no
	 * @throws ModelException en caso de que la búsqueda devuelva más de un resultado (si oneResultOnly es true)
	 * @throws ModelException en caso de que la búsqueda no devuelva resultados (si noResultThrowsExeption es true)
	 * @return el PO ya instanciado o null en caso de no encontrarlo (si noResultThrowsExeption es false)
	 */
	public PO getPO(String tableName, int poID, String columnName, String columnValue, boolean filterByOrg, boolean filterByClient, boolean oneResultOnly, boolean noResultThrowsException) throws ModelException
	{
		PO[] pos = getPOs(tableName, poID, columnName, columnValue, filterByOrg, filterByClient, oneResultOnly, noResultThrowsException);
		return pos != null ? pos[0] : null; 
	}

	/**
	 * Genera el String definitivo con la informacion recibida como parametros para la
	 * invocación del WS, bien sea mediante un ParameterBean o como un argumento del metodo
	 * @param data ParameterBean al cual se le invocará su toString() method.
	 * @return el String resultante con toda la información a mostrar en consola/log.
	 */
	protected String wsInvocationArguments(ParameterBean data) {
		StringBuffer out = new StringBuffer(data.toString());
		if (argNames != null && argValues != null && argNames.length > 0 && argNames.length == argValues.length) {
			out.append("\n - Method arguments: ");
			for (int i = 0; i < argNames.length; i++)
				out.append(argNames[i]).append(" = ").append(argValues[i]).append("; ");
		}
		return out.toString();
	}
	
	/**
	 * Busca el valor de una columna en una map dada 
	 * @param values la nomina de valores donde buscar
	 * @param columnName la columna a recuperar su valor
	 * @param lowerCaseMap si se desea pasar a minuscula la map
	 * @param propagateException si al presentarse un error se desea propagar la exception o no
	 * @return el valor correspondiente o -1 en CC
	 * @throws Exception en caso de no encontrar el valor y que propagateException sea true
	 */
	protected int getIntValueFromMap(HashMap<String, String> values, String columnName, boolean lowerCaseMap, boolean propagateException) throws Exception {
		// retorno por defecto
		int retValue = -1;
		// llevar a minusculas la map
		if (lowerCaseMap)
			values = toLowerCaseKeys(values);
		try {
			// intentar recuperar el valor
			retValue = Integer.parseInt(values.get(columnName));
		} catch (Exception e) {
			// propagar la excepcion?
			if (propagateException)
				throw new Exception(" Error al recuperar el valor de " + columnName + ": " + e.getMessage());
		}
		return retValue;

	}
	
	/**
	 * Retorna un timestamp si value es distinto a null, o null en caso contrario
	 * @param value a convertir en Timestamp
	 * @return el valor convertido a Timestamp
	 * @throws Exception en caso de un error en la conversión
	 */
	public Timestamp getTimestamp(String value) throws Exception {
		if (value==null)
			return null;
		try {
			return Timestamp.valueOf(value);
		} catch (Exception e) {
			throw new Exception("Error al convertir Timestamp con valor " + value + ". " + e.toString());
		}
	}
	
	
	/* ========================================== Implementacion de caches ========================================== */
	
	/** Cache de AD_Ref_Table: AD_Reference_ID -> AD_Table_ID */
	public static HashMap<Integer, Integer> refTable_referenceID_tableID = new HashMap<Integer, Integer>(); 
	/** Cache de AD_Ref_Table: AD_Reference_ID -> AD_Key */
	public static HashMap<Integer, Integer> refTable_referenceID_key = new HashMap<Integer, Integer>();	
	/** Cache de AD_Table: AD_Table_ID -> TableName */
	public static HashMap<Integer, String> 	table_tableID_tableName = new HashMap<Integer, String>();

	/** Cache: Retorna un tableID a partir de un referenceID utilizando la cache refTable_referenceID_tableID */
	public static int getTableIDFromReferenceID(int referenceID, String trxName) {
		if (refTable_referenceID_tableID.get(referenceID) == null) {
			String sql = "SELECT ad_table_id FROM ad_ref_table WHERE ad_reference_id = " + referenceID;
			int tableID = DB.getSQLValue(trxName, sql);
			refTable_referenceID_tableID.put(referenceID, tableID);
		}
		return refTable_referenceID_tableID.get(referenceID);
	}
	
	/** Cache: Retorna un key a partir de un referenceID utilizando la cache refTable_referenceID_key */
	public static int getKeyFromReferenceID(int referenceID, String trxName) {
		if (refTable_referenceID_key.get(referenceID) == null) {
			String sql = "SELECT ad_key FROM ad_ref_table WHERE ad_reference_id = " + referenceID;
			int key = DB.getSQLValue(trxName, sql);
			refTable_referenceID_key.put(referenceID, key);
		}
		return refTable_referenceID_key.get(referenceID);
	}
	
	/** Cache: Retorna un tableName a partir de un tableID utilizando la cache table_tableID_tableName */
	public static String getTableNameFromTableID(int tableID, String trxName) {
		if (table_tableID_tableName.get(tableID) == null) {
			String sql = "SELECT tablename FROM ad_table WHERE ad_table_id = ? LIMIT 1";
			String tableName = DB.getSQLValueString(trxName, sql, tableID);
			table_tableID_tableName.put(tableID, tableName);
		}
		return table_tableID_tableName.get(tableID);
	}

    
} 
 