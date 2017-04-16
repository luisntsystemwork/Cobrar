ALTER TABLE C_Order ADD Estado_Facturacion character varying(255);
ALTER TABLE C_Order ADD Estado_Pedido_Proveedor character varying(255);
--ALTER TABLE C_Order ADD Descripcion_Error character varying(255);

ALTER TABLE c_orderline ADD precioMaximoCompra numeric(22,4) NULL;
ALTER TABLE c_orderline ADD precioInformado numeric(22,4) NULL;

ALTER TABLE c_orderline ADD proveedor_id integer null;

ALTER TABLE c_orderline ADD CONSTRAINT proveedor_soline FOREIGN KEY (proveedor_id) REFERENCES c_bpartner(c_bpartner_id);
