ALTER TABLE C_Order ADD Estado_Facturacion character varying(255);
ALTER TABLE C_Order ADD Estado_Pedido_Proveedor character varying(255);

ALTER TABLE c_orderline ADD precioMaximoCompra numeric(22,4) NULL;
ALTER TABLE c_orderline ADD precioInformado numeric(22,4) NULL;