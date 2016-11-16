package org.libertya.ws.handler;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

import org.libertya.ws.bean.parameter.ParameterBean;
import org.libertya.ws.bean.result.ResultBean;
import org.libertya.ws.exception.ModelException;
import org.openXpertya.model.MProject;
import org.openXpertya.model.PO;
import org.openXpertya.util.CLogger;
import org.openXpertya.util.DB;
import org.openXpertya.util.Trx;

public class ProjectHandler extends DocumentHandler{

	public ResultBean projectUpdateByID(ParameterBean data, int projectID) {
		try
		{
			/* === Configuracion inicial === */
			init(data, new String[]{"C_Project_ID"}, new Object[]{projectID});
			
			/* === Procesar (logica especifica) === */	
			// Instanciar y persistir Invoice
			MProject mProject = (MProject)getPO("C_Project", projectID, null, null, false, false, true, true);
			setValues(mProject, data.getMainTable(), false);
			if (!mProject.save())
				throw new ModelException("Error al actualizar el proyecto:" + CLogger.retrieveErrorAsString());

			/* === Commitear transaccion === */
			Trx.getTrx(getTrxName()).commit();
			
			/* === Retornar valor === */
			HashMap<String, String> result = new HashMap<String, String>();
			return new ResultBean(false, null, result);
		}
		catch (ModelException me) {
			return processException(me, wsInvocationArguments(data));
		}
		catch (Exception e) {
			return processException(e, wsInvocationArguments(data));
		}
		finally	{
			closeTransaction();
		}
	}
	
	public ResultBean projectCreate(ParameterBean data)
	{
		try
		{
			/* === Configuracion inicial === */
			init(data, new String[]{}, new Object[]{});
			
			/* === Procesar (logica especifica) === */			
			// Instanciar y persistir BPartner
			//MProject newBPartner = new MProject(getCtx(), 0, getTrxName());
			
			String value = data.getMainTable().get("value");
			String name =         data.getMainTable().get("name");
			String datecontract = data.getMainTable().get("datecontract");
			String datefinish = data.getMainTable().get("datefinish");
			
			String clientID = data.getMainTable().get("ad_client_id");
			String orgID =    data.getMainTable().get("ad_org_id");
			String created =  data.getMainTable().get("created");
			String updated =  data.getMainTable().get("updated");
			
			guardarProyecto(clientID, orgID, created, updated, value, name, datecontract, datefinish);
			
			/* === Commitear transaccion === */
			//Trx.getTrx(getTrxName()).commit();
			int IdmProject = getIdMProjectByValue("C_Project", value);
			
			//Integer idProject = mProject.getC_Project_ID();
			
			guardarContabilidad(IdmProject, created, updated);
			
			/* === Retornar valor === */
			HashMap<String, String> result = new HashMap<String, String>();
			result.put("C_Project_ID", Integer.toString(IdmProject));
			return new ResultBean(false, null, result);
		}
		catch (ModelException me) {
			return processException(me, wsInvocationArguments(data));
		}
		catch (Exception e) {
			return processException(e, wsInvocationArguments(data));
		}
		finally	{
			closeTransaction();
		}
	}
	
	private int getIdMProjectByValue(String tableName, String columnValue) throws SQLException {
    			
		int[] retValue = PO.getAllIDs(tableName, "Value" + " = '" + columnValue + "'", getTrxName());
		
		if (retValue.length != 0)
			return retValue[0];
    	
		return 0;
	}
	
	private void guardarContabilidad(Integer idProyecto, String created, String updated) throws Exception {
		String sql = "INSERT INTO c_project_acct( "
				+ "c_project_id, c_acctschema_id, ad_client_id, ad_org_id, isactive, "
				+ "created, createdby, updated, updatedby, pj_asset_acct, pj_wip_acct) "
				+ "VALUES (<c_project_id>, <c_acctschema_id>, <ad_client_id>, <ad_org_id>, <isactive>, "
				        + "<created>, <createdby>, <updated>, <updatedby>, <pj_asset_acct>, <pj_wip_acct>);";
		sql = sql.replace("<c_project_id>", idProyecto.toString());
		sql = sql.replace("<c_acctschema_id>", "1010016");
		sql = sql.replace("<ad_client_id>", "1010016");
		sql = sql.replace("<ad_org_id>", "0");
		sql = sql.replace("<isactive>", "'Y'");
		sql = sql.replace("<created>", "'" + created + "'");
		sql = sql.replace("<createdby>", "100");
		sql = sql.replace("<updated>", "'" + updated + "'");
		sql = sql.replace("<updatedby>", "100");
		sql = sql.replace("<pj_asset_acct>", "1034399");
		sql = sql.replace("<pj_wip_acct>", "1034400");
		
		PreparedStatement pstmt = null;

        try {
            //pstmt = DB.prepareStatement( sql,
			//		ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE,
			//		getTrxName() );
            DB.executeUpdate( sql.toString(), getTrxName());
            //ResultSet rs = pstmt.executeQuery();
            
//            rs.close();
//            pstmt.close();
//            pstmt = null;
        } catch( Exception e ) {
            throw e;
        }

        finally {
        	try
        	{
	            if( pstmt != null ) {
	                pstmt.close();
	            }
	
	            pstmt = null;
        	}
        	catch( Exception e ) {
                pstmt = null;
            }
        }
	}

	private void guardarProyecto(String ad_client_id, String ad_org_id, String created, String updated, String value, String name, String datecontract, String datefinish) throws Exception 
	{
		String  sql  = "INSERT INTO c_project(" +
		          "  c_project_id, ad_client_id, ad_org_id, isactive, created, createdby, " +
		          "  updated, updatedby, \"value\", \"name\", description, note, issummary, " +
		          "  ad_user_id, c_bpartner_id, c_bpartner_location_id, poreference, " +
		          "  c_paymentterm_id, c_currency_id, m_pricelist_version_id, c_campaign_id, " +
		          "  iscommitment, plannedamt, plannedqty, plannedmarginamt, committedamt, " +
		          "  datecontract, datefinish, generateto, processed, salesrep_id, " +
		          "  copyfrom, c_projecttype_id, committedqty, invoicedamt, invoicedqty, " +
		          "  projectbalanceamt, c_phase_id, iscommitceiling, m_warehouse_id, " +
		          "  projectcategory, processing, createdocument) " +
		          " VALUES (nextval('seq_c_project'), <ad_client_id>, <ad_org_id>, 'Y', '<created>', 0, " +
		          "  '<updated>', <updatedby>, '<value>', '<name>', null, null, 'N', " +
		          "  null, null, null, null, " +
		          "  null, 118, null, null, " +
		          "  'Y', 0.00, 0.000, 0.00, 0.00, " +
		          "  '<datecontract>', '<datefinish>', null, 'N', null, " +
		          "  null, null, 0.00, 0.00, 0.00, " +
		          "  0.00, null, 'N', null, " +
		          "  'N', null, null);";
				
				sql = sql.replace("<ad_client_id>", ad_client_id);
				sql = sql.replace("<ad_org_id>",    ad_org_id);
				sql = sql.replace("<created>",      created);
				sql = sql.replace("<updated>",      updated);
				String updatedby = "100";
				sql = sql.replace("<updatedby>",      updatedby);
				
				
				sql = sql.replace("<value>",        value);
				sql = sql.replace("<name>",         name);
				sql = sql.replace("<datecontract>", datecontract);
				sql = sql.replace("<datefinish>",   datefinish);
		
        PreparedStatement pstmt = null;

        try {
            //pstmt = DB.prepareStatement( sql,
			//		ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE,
			//		getTrxName() );
            int no = DB.executeUpdate( sql.toString(), getTrxName());
            
            System.out.println(no);
            //ResultSet rs = pstmt.executeQuery();
            
//            rs.close();
//            pstmt.close();
//            pstmt = null;
        } catch( Exception e ) {
            throw e;
        }

        finally {
        	try
        	{
	            if( pstmt != null ) {
	                pstmt.close();
	            }
	
	            pstmt = null;
        	}
        	catch( Exception e ) {
                pstmt = null;
            }
        } 
		
	}
}
