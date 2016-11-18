/*
 *    El contenido de este fichero está sujeto a la  Licencia Pública openXpertya versión 1.1 (LPO)
 * en tanto en cuanto forme parte íntegra del total del producto denominado:  openXpertya, solución 
 * empresarial global , y siempre según los términos de dicha licencia LPO.
 *    Una copia  íntegra de dicha  licencia está incluida con todas  las fuentes del producto.
 *    Partes del código son CopyRight (c) 2002-2007 de Ingeniería Informática Integrada S.L., otras 
 * partes son  CopyRight (c) 2002-2007 de  Consultoría y  Soporte en  Redes y  Tecnologías  de  la
 * Información S.L.,  otras partes son  adaptadas, ampliadas,  traducidas, revisadas  y/o mejoradas
 * a partir de código original de  terceros, recogidos en el  ADDENDUM  A, sección 3 (A.3) de dicha
 * licencia  LPO,  y si dicho código es extraido como parte del total del producto, estará sujeto a
 * su respectiva licencia original.  
 *     Más información en http://www.openxpertya.org/ayuda/Licencia.html
 */



package org.openXpertya.apps.search;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.compiere.plaf.CompierePLAF;
import org.compiere.swing.CButton;
import org.compiere.swing.CLabel;
import org.compiere.swing.CTextField;
import org.openXpertya.apps.ADialog;
import org.openXpertya.apps.ALayout;
import org.openXpertya.apps.ALayoutConstraint;
import org.openXpertya.apps.search.helper.OrderInvoiceHelper;
import org.openXpertya.grid.ed.VCheckBox;
import org.openXpertya.grid.ed.VDate;
import org.openXpertya.grid.ed.VLookup;
import org.openXpertya.grid.ed.VNumber;
import org.openXpertya.minigrid.IDColumn;
import org.openXpertya.model.MLookup;
import org.openXpertya.model.MLookupFactory;
import org.openXpertya.model.MOrder;
import org.openXpertya.model.MQuery;
import org.openXpertya.model.M_Table;
import org.openXpertya.util.DB;
import org.openXpertya.util.DisplayType;
import org.openXpertya.util.Env;
import org.openXpertya.util.Msg;
import org.openXpertya.util.Util;

/**
 * Descripción de Clase
 *
 *
 * @version    2.2, 12.10.07
 * @author     Equipo de Desarrollo de openXpertya    
 */

public class InfoOrder extends Info {

    /**
     * Constructor de la clase ...
     *
     *
     * @param frame
     * @param modal
     * @param WindowNo
     * @param value
     * @param multiSelection
     * @param whereClause
     */

    public InfoOrder( Frame frame,boolean modal,int WindowNo,String value,boolean multiSelection,String whereClause ) {
        super( frame,modal,WindowNo,"o","C_Order_ID",multiSelection,whereClause );
        
        agregarBotonFacturas();
        
        
        log.info( "InfoOrder" );
        setTitle( Msg.getMsg( Env.getCtx(),"InfoOrder" ));

        //

        try {
            statInit();
            p_loadedOK = initInfo();
        } catch( Exception e ) {
            return;
        }

        //

        int no = p_table.getRowCount();

        setStatusLine( Integer.toString( no ) + " " + Msg.getMsg( Env.getCtx(),"SearchRows_EnterQuery" ),false );
        setStatusDB( Integer.toString( no ));

        if( (value != null) && (value.length() > 0) ) {
            fDocumentNo.setValue( value );
            executeQuery();
        }

        //

        pack();

        // Focus

        fDocumentNo.requestFocus();
        
        this.p_table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        this.p_table.getSelectionModel().addListSelectionListener(listSelectionListener);
    }    // InfoOrder
    
    private void agregarBotonFacturas() {
    	this.btnInvoice = confirmPanel.addButton( "FACTURAS","Facturas",Env.getImageIcon( "Caunt24.gif" ),KeyEvent.VK_I );
    	this.btnDelete = confirmPanel.addButton( "BORRAR","Borrar",Env.getImageIcon( "Delete24.gif" ),KeyEvent.VK_D);
    	this.enableButtons();
    	this.btnInvoice.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				
				System.out.println("Boton invoice");
				StringBuilder msgError = new StringBuilder();
				Boolean ordenesEnProcesoSeleccionadas = Boolean.FALSE;
				for (int r = 0; r < selectedRow.length; r++) {

	                IDColumn	id	= (IDColumn) p_table.getValueAt(selectedRow[r], 0);
	                Integer cOrderID = id.getRecord_ID();
	                OrderInvoiceHelper helper = new OrderInvoiceHelper();
	                try {
						MOrder mOrder = getPO("C_Order", cOrderID, MOrder.class);
						
						if (!helper.seDebeProcesar(mOrder)) {
							ordenesEnProcesoSeleccionadas = Boolean.TRUE;
							continue;
						}
						
						helper.habilitarOrdenParaBatch(mOrder);
						
	                } catch (Throwable e1) {
	                	msgError.append("No se pudieron procesar las Ordenes de trabajo " + p_table.getValueAt(selectedRow[r], 3) + ", por favor intente nuevamente más tarde \n");
						
					}
				}
				
				if (ordenesEnProcesoSeleccionadas)
					msgError.append("Las ordenes de trabajo seleccionadas ya se encuentran procesadas o rechazadas. Por favor verifique los datos generados\n");
	            
	            StringBuilder builder = new StringBuilder();
	            builder.append("Las Ordenes de trabajo seleccionadas se están procesando, consulte nuevamente en unos instantes para verificar el resultado de la operación\n");
	            if (msgError.length() != 0)
	            	builder.append(msgError.toString());
	            
	            ADialog.error(p_WindowNo,null,builder.toString());
	            
	            executeQuery();
			}

		});
    	
    	this.btnDelete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				
				System.out.println("Boton delete");
				StringBuilder msgError = new StringBuilder();
				for (int r = 0; r < selectedRow.length; r++) {

	                IDColumn	id	= (IDColumn) p_table.getValueAt(selectedRow[r], 0);
	                Integer cOrderID = id.getRecord_ID();
	                OrderInvoiceHelper helper = new OrderInvoiceHelper();
	                try {
						MOrder mOrder = getPO("C_Order", cOrderID, MOrder.class);
						
						if (!helper.sePuedeRechazar(mOrder))
							continue;
						
						helper.rechazarOrdenTrabajo(mOrder);
						
	                } catch (Throwable e1) {
	                	msgError.append("No se pudieron procesar las Ordenes de trabajo " + p_table.getValueAt(selectedRow[r], 3) + ", por favor intente nuevamente más tarde\n");
						
					}
				}
	            
	            StringBuilder builder = new StringBuilder();
	            builder.append("Se han rechazado correctamente las ordenes de trabajo  seleccionadas.\n");
	            if (msgError.length() != 0)
	            	builder.append(msgError.toString());
	            
	            ADialog.error(p_WindowNo,null,builder.toString());
	            
	            executeQuery();
			}

		});
    	
    	this.confirmPanel.addComponent(this.btnInvoice);
    	this.confirmPanel.addComponent(this.btnDelete);
    }

    /** Descripción de Campos */

    private Info_Column[] m_generalLayout;

    /** Descripción de Campos */

    private ArrayList m_queryColumns = new ArrayList();

    /** Descripción de Campos */

    private String m_tableName;

    /** Descripción de Campos */

    private String m_keyColumn;

    // Static Info

    /** Descripción de Campos */

    private CLabel lDocumentNo = new CLabel( Msg.translate( Env.getCtx(),"DocumentNo" ));

    /** Descripción de Campos */

    private CTextField fDocumentNo = new CTextField( 10 );

    /** Descripción de Campos */

    private CLabel lDescription = new CLabel( Msg.translate( Env.getCtx(),"Description" ));

    /** Descripción de Campos */

    private CTextField fDescription = new CTextField( 10 );

    /** Descripción de Campos */

    private CLabel lPOReference = new CLabel( Msg.translate( Env.getCtx(),"POReference" ));

    /** Descripción de Campos */

    private CTextField fPOReference = new CTextField( 10 );

    //
//      private CLabel lOrg_ID = new CLabel(Msg.translate(Env.getCtx(), "AD_Org_ID"));
//      private VLookup fOrg_ID;

    /** Descripción de Campos */

    private CLabel lBPartner_ID = new CLabel( Msg.translate( Env.getCtx(),"C_BPartner_ID" ));

    /** Descripción de Campos */

    private VLookup fBPartner_ID;

    //

    /** Descripción de Campos */

    private CLabel lDateFrom = new CLabel( Msg.translate( Env.getCtx(),"DateOrdered" ));

    /** Descripción de Campos */

    private VDate fDateFrom = new VDate( "DateFrom",false,false,true,DisplayType.Date,Msg.translate( Env.getCtx(),"DateFrom" ));

    /** Descripción de Campos */

    private CLabel lDateTo = new CLabel( "-" );

    /** Descripción de Campos */

    private VDate fDateTo = new VDate( "DateTo",false,false,true,DisplayType.Date,Msg.translate( Env.getCtx(),"DateTo" ));

    /** Descripción de Campos */

    private CLabel lAmtFrom = new CLabel( Msg.translate( Env.getCtx(),"GrandTotal" ));

    /** Descripción de Campos */

    private VNumber fAmtFrom = new VNumber( "AmtFrom",false,false,true,DisplayType.Amount,Msg.translate( Env.getCtx(),"AmtFrom" ));

    /** Descripción de Campos */

    private CLabel lAmtTo = new CLabel( "-" );

    /** Descripción de Campos */

    private VNumber fAmtTo = new VNumber( "AmtTo",false,false,true,DisplayType.Amount,Msg.translate( Env.getCtx(),"AmtTo" ));

    /** Descripción de Campos */

    private VCheckBox fIsSOTrx = new VCheckBox( "IsSOTrx",false,false,true,Msg.translate( Env.getCtx(),"IsSOTrx" ),"",false );

    /** Descripción de Campos */

    private static final Info_Column[] s_invoiceLayout = {
        new Info_Column( " ","o.C_Order_ID",IDColumn.class ),
        new Info_Column( Msg.translate( Env.getCtx(),"C_BPartner_ID" ), "(SELECT Name FROM C_BPartner bp WHERE bp.C_BPartner_ID=o.C_BPartner_ID)",String.class ),
        new Info_Column( Msg.translate( Env.getCtx(),"DateOrdered" ),   "o.DateOrdered",Timestamp.class ),
        new Info_Column( Msg.translate( Env.getCtx(),"DocumentNo" ),    "o.DocumentNo",String.class ),
        new Info_Column( Msg.translate( Env.getCtx(),"C_Currency_ID" ), "(SELECT ISO_Code FROM C_Currency c WHERE c.C_Currency_ID=o.C_Currency_ID)",String.class ),
        new Info_Column( Msg.translate( Env.getCtx(),"GrandTotal" ),    "o.GrandTotal",BigDecimal.class ),
        new Info_Column( Msg.translate( Env.getCtx(),"ConvertedAmount" ),"currencyBase(o.GrandTotal,o.C_Currency_ID,o.DateAcct, o.AD_Client_ID,o.AD_Org_ID)",BigDecimal.class ),
        new Info_Column( Msg.translate( Env.getCtx(),"IsSOTrx" ),    "o.IsSOTrx",Boolean.class ),
        new Info_Column( Msg.translate( Env.getCtx(),"Description" ),"o.Description",String.class ),
        new Info_Column( Msg.translate( Env.getCtx(),"POReference" ),"o.POReference",String.class ),
        ////
        new Info_Column( "Estado Facturacion" ,     "o.Estado_Facturacion",String.class )
       // new Info_Column( Msg.translate( Env.getCtx(),"Estado_Pedido_Proveedor" ),"o.Estado_Pedido_Proveedor",String.class )
    };
    
    private VLookup fEstadoFacturacion;
    
    private CLabel lEstadoFacturacion = new CLabel( "Estado Facturacion" );

    /**
     * Descripción de Método
     *
     *
     * @throws Exception
     */

    private void statInit() throws Exception {
        lDocumentNo.setLabelFor( fDocumentNo );
        fDocumentNo.setBackground( CompierePLAF.getInfoBackground());
        fDocumentNo.addActionListener( this );
        lDescription.setLabelFor( fDescription );
        fDescription.setBackground( CompierePLAF.getInfoBackground());
        fDescription.addActionListener( this );
        lPOReference.setLabelFor( lPOReference );
        fPOReference.setBackground( CompierePLAF.getInfoBackground());
        fPOReference.addActionListener( this );
        fIsSOTrx.setSelected( !"N".equals( Env.getContext( Env.getCtx(),p_WindowNo,"IsSOTrx" )));
        fIsSOTrx.addActionListener( this );

        //
        // fOrg_ID = new VLookup("AD_Org_ID", false, false, true,
        // MLookupFactory.create(Env.getCtx(), 3486, m_WindowNo, DisplayType.TableDir, false),
        // DisplayType.TableDir, m_WindowNo);
        // lOrg_ID.setLabelFor(fOrg_ID);
        // fOrg_ID.setBackground(CompierePLAF.getInfoBackground());
        fBPartner_ID = new VLookup( "C_BPartner_ID",false,false,true,MLookupFactory.get( Env.getCtx(),p_WindowNo,0,3499,DisplayType.Search ));
        lBPartner_ID.setLabelFor( fBPartner_ID );
        fBPartner_ID.setBackground( CompierePLAF.getInfoBackground());

        //

        lDateFrom.setLabelFor( fDateFrom );
        fDateFrom.setBackground( CompierePLAF.getInfoBackground());
        fDateFrom.setToolTipText( Msg.translate( Env.getCtx(),"DateFrom" ));
        lDateTo.setLabelFor( fDateTo );
        fDateTo.setBackground( CompierePLAF.getInfoBackground());
        fDateTo.setToolTipText( Msg.translate( Env.getCtx(),"DateTo" ));
        lAmtFrom.setLabelFor( fAmtFrom );
        fAmtFrom.setBackground( CompierePLAF.getInfoBackground());
        fAmtFrom.setToolTipText( Msg.translate( Env.getCtx(),"AmtFrom" ));
        lAmtTo.setLabelFor( fAmtTo );
        fAmtTo.setBackground( CompierePLAF.getInfoBackground());
        fAmtTo.setToolTipText( Msg.translate( Env.getCtx(),"AmtTo" ));
        
        MLookup m_estadoFacturacionNavicon = new MLookup( MLookupFactory.getLookup_List( Env.getLanguage( Env.getCtx()),1010281 ),0 );

        fEstadoFacturacion = new VLookup( "Estado_Facturacion",false,false,true,m_estadoFacturacionNavicon );
        lEstadoFacturacion.setLabelFor( fEstadoFacturacion );
        fEstadoFacturacion.setBackground( CompierePLAF.getInfoBackground());
        // Valor por defecto.
        fEstadoFacturacion.addActionListener( this );

        //

        parameterPanel.setLayout( new ALayout());

        // First Row

        parameterPanel.add( lDocumentNo,new ALayoutConstraint( 0,0 ));
        parameterPanel.add( fDocumentNo,null );
        parameterPanel.add( lBPartner_ID,null );
        parameterPanel.add( fBPartner_ID,null );
        parameterPanel.add( fIsSOTrx,new ALayoutConstraint( 0,5 ));

        // 2nd Row

        parameterPanel.add( lDescription,new ALayoutConstraint( 1,0 ));
        parameterPanel.add( fDescription,null );
        parameterPanel.add( lDateFrom,null );
        parameterPanel.add( fDateFrom,null );
        parameterPanel.add( lDateTo,null );
        parameterPanel.add( fDateTo,null );

        // 3rd Row

//        parameterPanel.add( lPOReference,new ALayoutConstraint( 2,0 ));
//        parameterPanel.add( fPOReference,null );
        parameterPanel.add( lEstadoFacturacion,new ALayoutConstraint( 2,0 ));
        parameterPanel.add( fEstadoFacturacion,null );
        parameterPanel.add( lAmtFrom,new ALayoutConstraint( 2,2 ) );
        parameterPanel.add( fAmtFrom,null );
        parameterPanel.add( lAmtTo,null );
        parameterPanel.add( fAmtTo,null );

        // parameterPanel.add(lOrg_ID, null);
        // parameterPanel.add(fOrg_ID, null);

    }    // statInit

    /**
     * Descripción de Método
     *
     *
     * @return
     */

    private boolean initInfo() {

        // Set Defaults

        String bp = Env.getContext( Env.getCtx(),p_WindowNo,"C_BPartner_ID" );

        if( (bp != null) && (bp.length() != 0) ) {
            fBPartner_ID.setValue( new Integer( bp ));
        }

        // prepare table

        StringBuffer where = new StringBuffer( "o.IsActive='Y'" );

        if( p_whereClause.length() > 0 ) {
            where.append( " AND " ).append( Util.replace( p_whereClause,"C_Order.","o." ));
        }
        // ORDENAMIENTO DE COLUMNAS
        // prepareTable( s_invoiceLayout," C_Order o",where.toString(),"2,3,4" );
        prepareTable( s_invoiceLayout," C_Order o",where.toString(),"3,4" );

        return true;
    }    // initInfo
    
    private CButton btnInvoice = new CButton(); /* Agregado */
    private CButton btnDelete = new CButton(); /* Agregado */
    
    String trxNameCreateInvoice = "createInvoice";
    
    @Override
    public void mouseClicked(MouseEvent e) {
    	if( (e.getClickCount() > 1) && (p_table.getSelectedRow() != -1) ) {
    		zoom();
    	}
    }
    
    private int getInvoiceDocTypeTargetID(int adClientID, int adOrgID) {
    	Integer docTypeID = 0;
    	try {
            String SQL = "SELECT c_doctype_id " 
            			+ "FROM C_DocType " 
            		    + "WHERE ad_client_id = " + adClientID    // 1
                         + " AND name like 'Factura B%'";
            PreparedStatement pstmt = DB.prepareStatement( SQL );

            // pstmt.setInt( 1,adClientID);

            ResultSet rs = pstmt.executeQuery();

            if( rs.next()) {
                
                docTypeID = rs.getInt(1);
            }

            rs.close();
            pstmt.close();
        } catch( SQLException e ) {
            log.log( Level.SEVERE,"c_doctype_id",e );

            return -1;
        }

        return docTypeID;
	}
    
    private <T> T getPO(String tableName, Integer id, Class<T> clazz) throws SQLException {
    	// Recuperar los registros de la bbdd 
		M_Table table = M_Table.get(Env.getCtx(), tableName);
		String sql = "SELECT * FROM " + tableName + " WHERE " + tableName + "_id = " + id;
		PreparedStatement ps = DB.prepareStatement(sql, tableName);
		ResultSet rs = ps.executeQuery();

		while (rs.next()) {
			return (T)table.getPO(rs, tableName);
		}
    	
		return null;
	}

	int[] selectedRow = null;
    
    private ActionListener buttonInvoiceListener = new ActionListener() {
		
    	public void actionPerformed(ActionEvent e) {
    		/*Integer x = getSelectedRowKey();
			if (x != null) {
				new InfoProductAttributeDetail(this, x, getWarehouseID()).setVisible(true);
			}*/
			System.out.println("Boton invoice");
			
			// int	rows[]	= p_table.getSelectedRows();

            for (int r = 0; r < selectedRow.length; r++) {

                IDColumn	id	= (IDColumn) p_table.getValueAt(selectedRow[r], 0);

                /*if (id != null) {

                    Integer	MPC_Order_ID	= id.getRecord_ID();
                    int		C_DocType_ID	= MMPCMRP.getDocType("MOP", false);
                    MMPCOrder	order		= new MMPCOrder(Env.getCtx(), MPC_Order_ID.intValue(), null);

                    order.setDocStatus(order.prepareIt());
                    order.setDocAction(order.DOCACTION_Complete);
                    order.save();
                }*/
            }
		}
	};
    
    private ListSelectionListener listSelectionListener = new ListSelectionListener() {

		public void valueChanged(ListSelectionEvent e) {
			selectedRow = p_table.getSelectedRows();
			
			for (int i = 0; i < selectedRow.length; i++) {
				System.out.println(selectedRow[i]);
			}
		}
    	
    };
    
    protected void enableButtons() {
    	super.enableButtons();
    	
    	boolean enable = p_table.getSelectedRow() != -1;
    	if (this.btnInvoice != null)
    		this.btnInvoice.setEnabled( enable );
    	if (this.btnDelete != null)
    		this.btnDelete.setEnabled( enable );
    }

    /**
     * Descripción de Método
     *
     *
     * @return
     */

    protected String getSQLWhere() {
        StringBuffer sql = new StringBuffer();

        if( fDocumentNo.getText().length() > 0 ) {
            sql.append( " AND UPPER(o.DocumentNo) LIKE ?" );
        }

        if( fDescription.getText().length() > 0 ) {
            sql.append( " AND UPPER(o.Description) LIKE ?" );
        }

        if( fPOReference.getText().length() > 0 ) {
            sql.append( " AND UPPER(o.POReference) LIKE ?" );
        }

        //

        if( fBPartner_ID.getValue() != null ) {
            sql.append( " AND o.C_BPartner_ID=?" );
        }
        
        if( fEstadoFacturacion.getValue() != null ) {
            sql.append( " AND UPPER(o.estado_facturacion) LIKE ?" );
        }

        //

        if( (fDateFrom.getValue() != null) || (fDateTo.getValue() != null) ) {
            Timestamp from = ( Timestamp )fDateFrom.getValue();
            Timestamp to   = ( Timestamp )fDateTo.getValue();

            if( (from == null) && (to != null) ) {
                sql.append( " AND TRUNC(o.DateOrdered) <= ?" );
            } else if( (from != null) && (to == null) ) {
                sql.append( " AND TRUNC(o.DateOrdered) >= ?" );
            } else if( (from != null) && (to != null) ) {
                sql.append( " AND TRUNC(o.DateOrdered) BETWEEN ? AND ?" );
            }
        }

        //

        if( (fAmtFrom.getValue() != null) || (fAmtTo.getValue() != null) ) {
            BigDecimal from = ( BigDecimal )fAmtFrom.getValue();
            BigDecimal to   = ( BigDecimal )fAmtTo.getValue();

            if( (from == null) && (to != null) ) {
                sql.append( " AND o.GrandTotal <= ?" );
            } else if( (from != null) && (to == null) ) {
                sql.append( " AND o.GrandTotal >= ?" );
            } else if( (from != null) && (to != null) ) {
                sql.append( " AND o.GrandTotal BETWEEN ? AND ?" );
            }
        }

        sql.append( " AND o.IsSOTrx=?" );
        log.finer( sql.toString());

        return sql.toString();
    }    // getSQLWhere

    /**
     * Descripción de Método
     *
     *
     * @param pstmt
     *
     * @throws SQLException
     */

    protected void setParameters( PreparedStatement pstmt ) throws SQLException {
        int index = 1;
        log.fine("En setPArameter del infoOrder con pstm= "+pstmt +" y con el index= "+ index);

        if( fDocumentNo.getText().length() > 0 ) {
            pstmt.setString( index++,getSQLText( fDocumentNo ));
        }

        if( fDescription.getText().length() > 0 ) {
            pstmt.setString( index++,getSQLText( fDescription ));
        }

        if( fPOReference.getText().length() > 0 ) {
            pstmt.setString( index++,getSQLText( fPOReference ));
        }

        //

        if( fBPartner_ID.getValue() != null ) {
            Integer bp = ( Integer )fBPartner_ID.getValue();

            pstmt.setInt( index++,bp.intValue());
            log.fine( "BPartner=" + bp );
        }
        
        if( fEstadoFacturacion.getValue() != null ) {
        	String estadoFacturacion = ( String ) fEstadoFacturacion.getValue();

            pstmt.setString( index++,estadoFacturacion);
            log.fine( "estado_facturacion LIKE " + estadoFacturacion );
        }

        //

        if( (fDateFrom.getValue() != null) || (fDateTo.getValue() != null) ) {
            Timestamp from = ( Timestamp )fDateFrom.getValue();
            Timestamp to   = ( Timestamp )fDateTo.getValue();

            log.fine( "Date From=" + from + ", To=" + to );

            if( (from == null) && (to != null) ) {
                pstmt.setTimestamp( index++,to );
            } else if( (from != null) && (to == null) ) {
                pstmt.setTimestamp( index++,from );
            } else if( (from != null) && (to != null) ) {
                pstmt.setTimestamp( index++,from );
                pstmt.setTimestamp( index++,to );
            }
        }

        //

        if( (fAmtFrom.getValue() != null) || (fAmtTo.getValue() != null) ) {
            BigDecimal from = ( BigDecimal )fAmtFrom.getValue();
            BigDecimal to   = ( BigDecimal )fAmtTo.getValue();

            log.fine( "Amt From=" + from + ", To=" + to );

            if( (from == null) && (to != null) ) {
                pstmt.setBigDecimal( index++,to );
            } else if( (from != null) && (to == null) ) {
                pstmt.setBigDecimal( index++,from );
            } else if( (from != null) && (to != null) ) {
                pstmt.setBigDecimal( index++,from );
                pstmt.setBigDecimal( index++,to );
            }
        }
        log.fine("En setPArameter  con el index= "+ index);

        pstmt.setString( index++,fIsSOTrx.isSelected()
                                 ?"Y"
                                 :"N" );
    }    // setParameters

    /**
     * Descripción de Método
     *
     *
     * @param f
     *
     * @return
     */

    private String getSQLText( CTextField f ) {
        String s = f.getText().toUpperCase();

        if( !s.endsWith( "%" )) {
            s += "%";
        }

        log.fine( "String=" + s );

        return s;
    }    // getSQLText

    /**
     * Descripción de Método
     *
     */

    void zoom() {
        log.info( "" );

        Integer C_Order_ID = getSelectedRowKey();

        if( C_Order_ID == null ) {
            return;
        }

        MQuery query = new MQuery( "C_Order" );

        query.addRestriction( "C_Order_ID",MQuery.EQUAL,C_Order_ID );

        int AD_WindowNo = getAD_Window_ID( "C_Order",fIsSOTrx.isSelected());

        zoom( AD_WindowNo,query );
    }    // zoom

    /**
     * Descripción de Método
     *
     *
     * @return
     */

    boolean hasZoom() {
        return true;
    }    // hasZoom
    
    
    @Override
	protected int getInfoWidth() {
    	return INFO_WIDTH+240;
    }
}    // InfoOrder



/*
 *  @(#)InfoOrder.java   02.07.07
 * 
 *  Fin del fichero InfoOrder.java
 *  
 *  Versión 2.2
 *
 */
