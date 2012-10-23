-- MySQL dump 10.13  Distrib 5.5.27, for debian-linux-gnu (i686)
--
-- Host: localhost    Database: openboxes_dev
-- ------------------------------------------------------
-- Server version	5.5.27-0ubuntu2

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `address`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `address` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `address` varchar(255) NOT NULL,
  `address2` varchar(255) NOT NULL,
  `city` varchar(255) NOT NULL,
  `country` varchar(255) NOT NULL,
  `date_created` datetime NOT NULL,
  `last_updated` datetime NOT NULL,
  `postal_code` varchar(255) DEFAULT NULL,
  `state_or_province` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `address`
--

LOCK TABLES `address` WRITE;
/*!40000 ALTER TABLE `address` DISABLE KEYS */;
INSERT INTO `address` VALUES ('1',0,'888 Commonwealth Avenue','Third Floor','Boston','United States','2010-08-25 00:00:00','2010-08-25 00:00:00','02215','MA'),('2',0,'1000 State Street','Building A','Miami','United States','2010-08-25 00:00:00','2010-08-25 00:00:00','33126','FL'),('3',0,'12345 Main Street','Suite 401','Tabarre','Haiti','2010-08-25 00:00:00','2010-08-25 00:00:00',NULL,NULL),('4',0,'2482 Massachusetts Ave','','Boston','United Status','2010-08-25 00:00:00','2010-08-25 00:00:00','02215','MA');
/*!40000 ALTER TABLE `address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attribute`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `attribute` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `last_updated` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `allow_other` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attribute`
--

LOCK TABLES `attribute` WRITE;
/*!40000 ALTER TABLE `attribute` DISABLE KEYS */;
/*!40000 ALTER TABLE `attribute` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attribute_options`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `attribute_options` (
  `attribute_id` char(38) DEFAULT NULL,
  `options_string` varchar(255) DEFAULT NULL,
  `options_idx` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attribute_options`
--

LOCK TABLES `attribute_options` WRITE;
/*!40000 ALTER TABLE `attribute_options` DISABLE KEYS */;
/*!40000 ALTER TABLE `attribute_options` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `category` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `last_updated` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `parent_category_id` char(38) DEFAULT NULL,
  `sort_order` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK302BCFE619A2EF8` (`parent_category_id`),
  CONSTRAINT `FK302BCFE619A2EF8` FOREIGN KEY (`parent_category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES ('1',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Medicines',NULL,0),('10',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Surgical Equipment','3',0),('11',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','IT Equipment','3',0),('12',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Furniture and Equipment','3',0),('13',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Food','4',0),('14',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','ARVS','1',0),('15',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Anesteshia','1',0),('16',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Cancer','1',0),('17',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Chronic Care','1',0),('18',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Pain','1',0),('19',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','TB','1',0),('2',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Supplies',NULL,0),('20',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Other','1',0),('21',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Lab','6',0),('22',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Surgical','6',0),('23',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','X-Ray','6',0),('24',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Dental','6',0),('25',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Other','6',0),('3',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Equipment',NULL,0),('4',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Perishables',NULL,0),('5',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Other',NULL,0),('6',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Medical Supplies','2',0),('7',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Hospital and Clinic Supplies','2',0),('8',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Office Supplies','2',0),('9',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Medical Equipment','3',0);
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `comment` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `comment` text,
  `date_created` datetime NOT NULL,
  `date_read` datetime DEFAULT NULL,
  `date_sent` datetime DEFAULT NULL,
  `last_updated` datetime NOT NULL,
  `recipient_id` char(38) DEFAULT NULL,
  `sender_id` char(38) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK38A5EE5FAF1302EB` (`sender_id`),
  KEY `FK38A5EE5FF885F087` (`recipient_id`),
  CONSTRAINT `FK38A5EE5FAF1302EB` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK38A5EE5FF885F087` FOREIGN KEY (`recipient_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
INSERT INTO `comment` VALUES ('1',0,'We need to ship this as soon as possible!','2010-08-25 00:00:00',NULL,NULL,'2010-08-25 00:00:00','3','3'),('2',0,'Did you ship this yet?!?!?!?','2010-08-25 00:00:00',NULL,NULL,'2010-08-25 00:00:00','3','2'),('3',0,'What is taking so long?','2010-08-25 00:00:00',NULL,NULL,'2010-08-25 00:00:00','3','1');
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `consumption`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `consumption` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `day` int(11) DEFAULT NULL,
  `inventory_item_id` char(38) DEFAULT NULL,
  `month` int(11) DEFAULT NULL,
  `product_id` char(38) DEFAULT NULL,
  `year` int(11) DEFAULT NULL,
  `location_id` char(38) DEFAULT NULL,
  `transaction_date` datetime DEFAULT NULL,
  `date_created` datetime DEFAULT NULL,
  `last_updated` datetime DEFAULT NULL,
  `quantity` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKCD71F39B8ABEBD5` (`location_id`),
  KEY `FKCD71F39BAA992CED` (`inventory_item_id`),
  KEY `FKCD71F39BDED5FAE7` (`product_id`),
  CONSTRAINT `FKCD71F39BAA992CED` FOREIGN KEY (`inventory_item_id`) REFERENCES `inventory_item` (`id`),
  CONSTRAINT `FKCD71F39B8ABEBD5` FOREIGN KEY (`location_id`) REFERENCES `location` (`id`),
  CONSTRAINT `FKCD71F39BDED5FAE7` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consumption`
--

LOCK TABLES `consumption` WRITE;
/*!40000 ALTER TABLE `consumption` DISABLE KEYS */;
/*!40000 ALTER TABLE `consumption` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `container`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `container` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `container_number` varchar(255) DEFAULT NULL,
  `container_type_id` char(38) DEFAULT NULL,
  `date_created` datetime NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `height` float(12,3) DEFAULT NULL,
  `last_updated` datetime NOT NULL,
  `length` float(12,3) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `parent_container_id` char(38) DEFAULT NULL,
  `recipient_id` char(38) DEFAULT NULL,
  `shipment_id` char(38) DEFAULT NULL,
  `volume_units` varchar(255) DEFAULT NULL,
  `weight` float(12,3) DEFAULT NULL,
  `weight_units` varchar(255) DEFAULT NULL,
  `width` float(12,3) DEFAULT NULL,
  `container_status` varchar(255) DEFAULT NULL,
  `sort_order` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKE7814C8117A6E251` (`container_type_id`),
  KEY `FKE7814C813B5F6286` (`shipment_id`),
  KEY `FKE7814C8144979D51` (`recipient_id`),
  KEY `FKE7814C814B6A2E03` (`parent_container_id`),
  CONSTRAINT `FKE7814C814B6A2E03` FOREIGN KEY (`parent_container_id`) REFERENCES `container` (`id`),
  CONSTRAINT `FKE7814C8117A6E251` FOREIGN KEY (`container_type_id`) REFERENCES `container_type` (`id`),
  CONSTRAINT `FKE7814C813B5F6286` FOREIGN KEY (`shipment_id`) REFERENCES `shipment` (`id`),
  CONSTRAINT `FKE7814C8144979D51` FOREIGN KEY (`recipient_id`) REFERENCES `person` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `container`
--

LOCK TABLES `container` WRITE;
/*!40000 ALTER TABLE `container` DISABLE KEYS */;
/*!40000 ALTER TABLE `container` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `container_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `container_type` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `last_updated` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `sort_order` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `container_type`
--

LOCK TABLES `container_type` WRITE;
/*!40000 ALTER TABLE `container_type` DISABLE KEYS */;
INSERT INTO `container_type` VALUES ('1',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Container|fr:Conteneur',0),('2',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Pallet|fr:Palette',0),('3',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Suitcase|fr:Valise/Malette',0),('4',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Box|fr:Boite',0),('5',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Trunk|fr:Coffre',0),('6',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Item|fr:Element',0),('7',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Other|fr:Autre',0),('8',0,'2011-02-25 00:00:00',NULL,'2011-02-25 00:00:00','Crate|fr:Caisse',0);
/*!40000 ALTER TABLE `container_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `document`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `document` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `content_type` varchar(255) DEFAULT NULL,
  `date_created` datetime NOT NULL,
  `document_number` varchar(255) DEFAULT NULL,
  `document_type_id` char(38) DEFAULT NULL,
  `extension` varchar(255) DEFAULT NULL,
  `file_contents` mediumblob,
  `file_uri` tinyblob,
  `filename` varchar(255) DEFAULT NULL,
  `last_updated` datetime NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK335CD11B6631D8CC` (`document_type_id`),
  CONSTRAINT `FK335CD11B6631D8CC` FOREIGN KEY (`document_type_id`) REFERENCES `document_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `document`
--

LOCK TABLES `document` WRITE;
/*!40000 ALTER TABLE `document` DISABLE KEYS */;
INSERT INTO `document` VALUES ('1',0,'text/plain','2010-08-25 00:00:00',NULL,'3',NULL,'This page intentionally left blank.',NULL,'packing-list.pdf','2010-08-25 00:00:00',NULL),('2',0,'text/plain','2010-08-25 00:00:00',NULL,'5',NULL,'This page intentionally left blank.',NULL,'invoice.pdf','2010-08-25 00:00:00',NULL);
/*!40000 ALTER TABLE `document` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `document_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `document_type` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `last_updated` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `sort_order` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `document_type`
--

LOCK TABLES `document_type` WRITE;
/*!40000 ALTER TABLE `document_type` DISABLE KEYS */;
INSERT INTO `document_type` VALUES ('1',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Facture donnée par la ligne aérienne|fr:Conteneur',0),('2',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Bill of Lading|fr:Facture de la cargaison',0),('3',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Packing List|fr:Lisde de chargement',0),('4',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Certificate of Donation|fr:Certificat de don',0),('5',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Commercial Invoice|fr:Facture Commerciale',0),('6',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Material Safey Data Sheet|fr:Fiche signalétique',0),('7',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Certificate of Analysis|fr:Certificat d\'analyse',0),('8',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Manifest|fr:Manifeste',0),('9',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Other|fr:Autre',0);
/*!40000 ALTER TABLE `document_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `donor`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `donor` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `date_created` datetime DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `last_updated` datetime DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `donor`
--

LOCK TABLES `donor` WRITE;
/*!40000 ALTER TABLE `donor` DISABLE KEYS */;
INSERT INTO `donor` VALUES ('1',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Donor Organization ABC'),('2',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Donor Organization XYZ'),('3',0,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Donor Organization 123');
/*!40000 ALTER TABLE `donor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `event`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `event` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `event_date` datetime DEFAULT NULL,
  `event_location_id` char(38) DEFAULT NULL,
  `event_type_id` char(38) DEFAULT NULL,
  `last_updated` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5C6729A4415A5B0` (`event_location_id`),
  KEY `FK5C6729A3D970DB4` (`event_type_id`),
  CONSTRAINT `FK5C6729A4415A5B0` FOREIGN KEY (`event_location_id`) REFERENCES `location` (`id`),
  CONSTRAINT `FK5C6729A3D970DB4` FOREIGN KEY (`event_type_id`) REFERENCES `event_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `event`
--

LOCK TABLES `event` WRITE;
/*!40000 ALTER TABLE `event` DISABLE KEYS */;
/*!40000 ALTER TABLE `event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `event_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `event_type` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `event_code` varchar(255) DEFAULT NULL,
  `last_updated` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `sort_order` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `event_type`
--

LOCK TABLES `event_type` WRITE;
/*!40000 ALTER TABLE `event_type` DISABLE KEYS */;
INSERT INTO `event_type` VALUES ('2',0,'2011-03-03 00:00:00','Shipment has been shipped','SHIPPED','2011-03-03 00:00:00','Shipped|fr:Expédié',2),('3',0,'2011-03-03 00:00:00','Shipment has been received','RECEIVED','2011-03-03 00:00:00','Received|fr:Reçu',3);
/*!40000 ALTER TABLE `event_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fulfillment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fulfillment` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `date_fulfilled` datetime DEFAULT NULL,
  `fulfilled_by_id` char(38) DEFAULT NULL,
  `last_updated` datetime NOT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5A2551DEAC392B33` (`fulfilled_by_id`),
  CONSTRAINT `FK5A2551DEAC392B33` FOREIGN KEY (`fulfilled_by_id`) REFERENCES `person` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fulfillment`
--

LOCK TABLES `fulfillment` WRITE;
/*!40000 ALTER TABLE `fulfillment` DISABLE KEYS */;
/*!40000 ALTER TABLE `fulfillment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fulfillment_item`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fulfillment_item` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `fulfillment_id` char(38) DEFAULT NULL,
  `inventory_item_id` char(38) DEFAULT NULL,
  `last_updated` datetime NOT NULL,
  `quantity` int(11) DEFAULT NULL,
  `request_item_id` char(38) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKEDC55CD447EBE106` (`request_item_id`),
  KEY `FKEDC55CD494567276` (`fulfillment_id`),
  KEY `FKEDC55CD4AA992CED` (`inventory_item_id`),
  CONSTRAINT `FKEDC55CD4AA992CED` FOREIGN KEY (`inventory_item_id`) REFERENCES `inventory_item` (`id`),
  CONSTRAINT `FKEDC55CD447EBE106` FOREIGN KEY (`request_item_id`) REFERENCES `request_item` (`id`),
  CONSTRAINT `FKEDC55CD494567276` FOREIGN KEY (`fulfillment_id`) REFERENCES `fulfillment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fulfillment_item`
--

LOCK TABLES `fulfillment_item` WRITE;
/*!40000 ALTER TABLE `fulfillment_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `fulfillment_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fulfillment_item_shipment_item`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fulfillment_item_shipment_item` (
  `fulfillment_item_shipment_items_id` char(38) DEFAULT NULL,
  `shipment_item_id` char(38) DEFAULT NULL,
  KEY `FKE071DE6DB06EC4FB` (`shipment_item_id`),
  KEY `FKE071DE6DB42751E1` (`fulfillment_item_shipment_items_id`),
  CONSTRAINT `FKE071DE6DB06EC4FB` FOREIGN KEY (`shipment_item_id`) REFERENCES `shipment_item` (`id`),
  CONSTRAINT `FKE071DE6DB42751E1` FOREIGN KEY (`fulfillment_item_shipment_items_id`) REFERENCES `fulfillment_item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fulfillment_item_shipment_item`
--

LOCK TABLES `fulfillment_item_shipment_item` WRITE;
/*!40000 ALTER TABLE `fulfillment_item_shipment_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `fulfillment_item_shipment_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inventory` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `last_inventory_date` datetime DEFAULT NULL,
  `date_created` datetime DEFAULT NULL,
  `last_updated` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory`
--

LOCK TABLES `inventory` WRITE;
/*!40000 ALTER TABLE `inventory` DISABLE KEYS */;
INSERT INTO `inventory` VALUES ('1',0,NULL,'2011-02-17 00:00:00','2011-02-17 00:00:00'),('ff8081813a512a91013a512f140a0001',0,NULL,'2012-10-11 14:56:10','2012-10-11 14:56:10');
/*!40000 ALTER TABLE `inventory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory_item`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inventory_item` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `last_updated` datetime NOT NULL,
  `lot_number` varchar(255) DEFAULT NULL,
  `product_id` char(38) DEFAULT NULL,
  `expiration_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `product_id` (`product_id`,`lot_number`),
  KEY `FKFE019416DED5FAE7` (`product_id`),
  CONSTRAINT `FKFE019416DED5FAE7` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory_item`
--

LOCK TABLES `inventory_item` WRITE;
/*!40000 ALTER TABLE `inventory_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `inventory_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory_level`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inventory_level` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `inventory_id` char(38) DEFAULT NULL,
  `min_quantity` int(11) DEFAULT NULL,
  `product_id` char(38) DEFAULT NULL,
  `reorder_quantity` int(11) DEFAULT NULL,
  `date_created` datetime DEFAULT NULL,
  `last_updated` datetime DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKC254A2E1DED5FAE7` (`product_id`),
  KEY `FKC254A2E172A2C5B4` (`inventory_id`),
  CONSTRAINT `FKC254A2E1DED5FAE7` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `FKC254A2E172A2C5B4` FOREIGN KEY (`inventory_id`) REFERENCES `inventory` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory_level`
--

LOCK TABLES `inventory_level` WRITE;
/*!40000 ALTER TABLE `inventory_level` DISABLE KEYS */;
/*!40000 ALTER TABLE `inventory_level` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `local_transfer`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `local_transfer` (
  `id` char(38) NOT NULL DEFAULT '',
  `source_transaction_id` char(38) DEFAULT NULL,
  `destination_transaction_id` char(38) DEFAULT NULL,
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `last_updated` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7975323F4CC49445` (`destination_transaction_id`),
  KEY `FK7975323F57563498` (`source_transaction_id`),
  CONSTRAINT `FK7975323F4CC49445` FOREIGN KEY (`destination_transaction_id`) REFERENCES `transaction` (`id`),
  CONSTRAINT `FK7975323F57563498` FOREIGN KEY (`source_transaction_id`) REFERENCES `transaction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `local_transfer`
--

LOCK TABLES `local_transfer` WRITE;
/*!40000 ALTER TABLE `local_transfer` DISABLE KEYS */;
/*!40000 ALTER TABLE `local_transfer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `location`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `location` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `address_id` char(38) DEFAULT NULL,
  `date_created` datetime NOT NULL,
  `last_updated` datetime NOT NULL,
  `logo_url` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `inventory_id` char(38) DEFAULT NULL,
  `manager_id` char(38) DEFAULT NULL,
  `logo` mediumblob,
  `managed_locally` bit(1) DEFAULT NULL,
  `active` bit(1) DEFAULT NULL,
  `location_type_id` char(38) DEFAULT NULL,
  `parent_location_id` char(38) DEFAULT NULL,
  `local` bit(1) DEFAULT NULL,
  `bg_color` varchar(255) DEFAULT NULL,
  `fg_color` varchar(255) DEFAULT NULL,
  `location_group_id` char(38) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK714F9FB528F75F00` (`location_type_id`),
  KEY `FK714F9FB541E07A73` (`manager_id`),
  KEY `FK714F9FB572A2C5B4` (`inventory_id`),
  KEY `FK714F9FB57AF9A3C0` (`parent_location_id`),
  KEY `FK714F9FB53BB36E94` (`location_group_id`),
  KEY `FK714F9FB561ED379F` (`address_id`),
  CONSTRAINT `FK714F9FB57AF9A3C0` FOREIGN KEY (`parent_location_id`) REFERENCES `location` (`id`),
  CONSTRAINT `FK714F9FB528F75F00` FOREIGN KEY (`location_type_id`) REFERENCES `location_type` (`id`),
  CONSTRAINT `FK714F9FB53BB36E94` FOREIGN KEY (`location_group_id`) REFERENCES `location_group` (`id`),
  CONSTRAINT `FK714F9FB541E07A73` FOREIGN KEY (`manager_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK714F9FB561ED379F` FOREIGN KEY (`address_id`) REFERENCES `address` (`id`),
  CONSTRAINT `FK714F9FB572A2C5B4` FOREIGN KEY (`inventory_id`) REFERENCES `inventory` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location`
--

LOCK TABLES `location` WRITE;
/*!40000 ALTER TABLE `location` DISABLE KEYS */;
INSERT INTO `location` VALUES ('1',0,'1','2010-08-25 00:00:00','2010-08-25 00:00:00','http://a3.twimg.com/profile_images/134665083/BOS_Red_Sox_normal.PNG','Boston Headquarters','1','2',NULL,'','','2',NULL,'',NULL,NULL,NULL),('2',0,'2','2010-08-25 00:00:00','2010-08-25 00:00:00','http://pihemr.files.wordpress.com/2008/01/pih-hands.jpg','Miami Warehouse',NULL,'2',NULL,'','','2',NULL,'',NULL,NULL,NULL),('3',0,'3','2010-08-25 00:00:00','2010-08-25 00:00:00','http://pihemr.files.wordpress.com/2008/01/pih-hands.jpg','Tabarre Depot',NULL,'2',NULL,'','','2',NULL,'',NULL,NULL,NULL),('4',1,'4','2010-08-25 00:00:00','2012-10-11 14:56:10','http://pihemr.files.wordpress.com/2008/01/pih-hands.jpg','ZZZ Supply Company','ff8081813a512a91013a512f140a0001','2',NULL,'','','2',NULL,'',NULL,NULL,NULL);
/*!40000 ALTER TABLE `location` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `location_group`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `location_group` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `last_updated` datetime NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location_group`
--

LOCK TABLES `location_group` WRITE;
/*!40000 ALTER TABLE `location_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `location_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `location_supported_activities`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `location_supported_activities` (
  `location_id` char(38) DEFAULT NULL,
  `supported_activities_string` varchar(255) DEFAULT NULL,
  KEY `FKF58372688ABEBD5` (`location_id`),
  CONSTRAINT `FKF58372688ABEBD5` FOREIGN KEY (`location_id`) REFERENCES `location` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location_supported_activities`
--

LOCK TABLES `location_supported_activities` WRITE;
/*!40000 ALTER TABLE `location_supported_activities` DISABLE KEYS */;
/*!40000 ALTER TABLE `location_supported_activities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `location_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `location_type` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `last_updated` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `sort_order` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location_type`
--

LOCK TABLES `location_type` WRITE;
/*!40000 ALTER TABLE `location_type` DISABLE KEYS */;
INSERT INTO `location_type` VALUES ('2',0,'2010-12-06 00:00:00','Depot','2010-12-06 00:00:00','Depot|fr:Dépot',0),('3',0,'2010-12-06 00:00:00','Dispensary','2011-11-14 00:00:00','Dispensary|fr:Dispensaire',0),('4',0,'2010-12-06 00:00:00','Supplier','2010-12-06 00:00:00','Supplier|fr:Fournisseurs',0);
/*!40000 ALTER TABLE `location_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `location_type_supported_activities`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `location_type_supported_activities` (
  `location_type_id` char(38) DEFAULT NULL,
  `supported_activities_string` varchar(255) DEFAULT NULL,
  KEY `FK7AFF67F928F75F00` (`location_type_id`),
  CONSTRAINT `FK7AFF67F928F75F00` FOREIGN KEY (`location_type_id`) REFERENCES `location_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location_type_supported_activities`
--

LOCK TABLES `location_type_supported_activities` WRITE;
/*!40000 ALTER TABLE `location_type_supported_activities` DISABLE KEYS */;
INSERT INTO `location_type_supported_activities` VALUES ('2','MANAGE_INVENTORY'),('2','PLACE_ORDER'),('2','PLACE_REQUEST'),('2','FULFILL_REQUEST'),('2','SEND_STOCK'),('2','RECEIVE_STOCK'),('2','EXTERNAL'),('3','SEND_STOCK'),('3','RECEIVE_STOCK'),('4','FULFILL_ORDER'),('4','SEND_STOCK'),('4','EXTERNAL');
/*!40000 ALTER TABLE `location_type_supported_activities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `date_created` datetime DEFAULT NULL,
  `date_ordered` varchar(255) DEFAULT NULL,
  `description` varchar(255) NOT NULL,
  `destination_id` char(38) DEFAULT NULL,
  `last_updated` datetime DEFAULT NULL,
  `order_number` varchar(255) DEFAULT NULL,
  `ordered_by_id` char(38) DEFAULT NULL,
  `origin_id` char(38) DEFAULT NULL,
  `recipient_id` char(38) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK651874E1E2B3CDC` (`destination_id`),
  KEY `FK651874E44979D51` (`recipient_id`),
  KEY `FK651874E635BDB37` (`ordered_by_id`),
  KEY `FK651874EDBDEDAC4` (`origin_id`),
  CONSTRAINT `FK651874EDBDEDAC4` FOREIGN KEY (`origin_id`) REFERENCES `location` (`id`),
  CONSTRAINT `FK651874E1E2B3CDC` FOREIGN KEY (`destination_id`) REFERENCES `location` (`id`),
  CONSTRAINT `FK651874E44979D51` FOREIGN KEY (`recipient_id`) REFERENCES `person` (`id`),
  CONSTRAINT `FK651874EAF6D8801` FOREIGN KEY (`ordered_by_id`) REFERENCES `person` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order`
--

LOCK TABLES `order` WRITE;
/*!40000 ALTER TABLE `order` DISABLE KEYS */;
/*!40000 ALTER TABLE `order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_comment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_comment` (
  `order_comments_id` char(38) DEFAULT NULL,
  `comment_id` char(38) DEFAULT NULL,
  KEY `FK2DE9EE6EB8839C0F` (`order_comments_id`),
  KEY `FK2DE9EE6EC4A49BBF` (`comment_id`),
  CONSTRAINT `FK2DE9EE6EB8839C0F` FOREIGN KEY (`order_comments_id`) REFERENCES `order` (`id`),
  CONSTRAINT `FK2DE9EE6EC4A49BBF` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_comment`
--

LOCK TABLES `order_comment` WRITE;
/*!40000 ALTER TABLE `order_comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_document`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_document` (
  `order_documents_id` char(38) DEFAULT NULL,
  `document_id` char(38) DEFAULT NULL,
  KEY `FKE698D2ECC800AA15` (`document_id`),
  KEY `FKE698D2ECFE10118D` (`order_documents_id`),
  CONSTRAINT `FKE698D2ECC800AA15` FOREIGN KEY (`document_id`) REFERENCES `document` (`id`),
  CONSTRAINT `FKE698D2ECFE10118D` FOREIGN KEY (`order_documents_id`) REFERENCES `order` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_document`
--

LOCK TABLES `order_document` WRITE;
/*!40000 ALTER TABLE `order_document` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_document` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_event`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_event` (
  `order_events_id` char(38) DEFAULT NULL,
  `event_id` char(38) DEFAULT NULL,
  KEY `FK74D92A69786431F` (`event_id`),
  KEY `FK74D92A693D2E628A` (`order_events_id`),
  CONSTRAINT `FK74D92A69786431F` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`),
  CONSTRAINT `FK74D92A693D2E628A` FOREIGN KEY (`order_events_id`) REFERENCES `order` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_event`
--

LOCK TABLES `order_event` WRITE;
/*!40000 ALTER TABLE `order_event` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_item`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_item` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `category_id` char(38) DEFAULT NULL,
  `date_created` datetime NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `inventory_item_id` char(38) DEFAULT NULL,
  `last_updated` datetime NOT NULL,
  `order_id` char(38) DEFAULT NULL,
  `product_id` char(38) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
  `requested_by_id` char(38) DEFAULT NULL,
  `unit_price` float(12,0) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2D110D64911E7578` (`requested_by_id`),
  KEY `FK2D110D64AA992CED` (`inventory_item_id`),
  KEY `FK2D110D64D08EDBE6` (`order_id`),
  KEY `FK2D110D64DED5FAE7` (`product_id`),
  KEY `FK2D110D64EF4C770D` (`category_id`),
  CONSTRAINT `FK2D110D64EF4C770D` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`),
  CONSTRAINT `FK2D110D64911E7578` FOREIGN KEY (`requested_by_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK2D110D64AA992CED` FOREIGN KEY (`inventory_item_id`) REFERENCES `inventory_item` (`id`),
  CONSTRAINT `FK2D110D64D08EDBE6` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`),
  CONSTRAINT `FK2D110D64DED5FAE7` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_item`
--

LOCK TABLES `order_item` WRITE;
/*!40000 ALTER TABLE `order_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_item_comment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_item_comment` (
  `order_item_comments_id` char(38) DEFAULT NULL,
  `comment_id` char(38) DEFAULT NULL,
  KEY `FKB5A4FE84C4A49BBF` (`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_item_comment`
--

LOCK TABLES `order_item_comment` WRITE;
/*!40000 ALTER TABLE `order_item_comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_item_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_shipment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_shipment` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `order_item_id` char(38) DEFAULT NULL,
  `shipment_item_id` char(38) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9475736B3BE9D843` (`order_item_id`),
  KEY `FK9475736BB06EC4FB` (`shipment_item_id`),
  CONSTRAINT `FK9475736B3BE9D843` FOREIGN KEY (`order_item_id`) REFERENCES `order_item` (`id`),
  CONSTRAINT `FK9475736BB06EC4FB` FOREIGN KEY (`shipment_item_id`) REFERENCES `shipment_item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_shipment`
--

LOCK TABLES `order_shipment` WRITE;
/*!40000 ALTER TABLE `order_shipment` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_shipment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `person`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `person` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `last_updated` datetime NOT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `person`
--

LOCK TABLES `person` WRITE;
/*!40000 ALTER TABLE `person` DISABLE KEYS */;
INSERT INTO `person` VALUES ('1',1,'2010-08-25 00:00:00','admin@pih.org','Miss','Administrator','2010-08-25 00:00:00',NULL),('2',5,'2010-08-25 00:00:00','manager@pih.org','Mister','Manager','2010-08-25 00:00:00',NULL),('3',1,'2010-08-25 00:00:00','jmiranda@pih.org','Justin','Miranda','2010-08-25 00:00:00',NULL),('4',1,'2010-08-25 00:00:00','inactive@pih.org','In','Active','2010-08-25 00:00:00',NULL);
/*!40000 ALTER TABLE `person` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `picklist`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `picklist` (
  `id` char(38) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `request_id` char(38) DEFAULT NULL,
  `picker_id` char(38) DEFAULT NULL,
  `date_picked` datetime DEFAULT NULL,
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `last_updated` datetime NOT NULL,
  `created_by_id` char(38) DEFAULT NULL,
  `updated_by_id` char(38) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `picklist`
--

LOCK TABLES `picklist` WRITE;
/*!40000 ALTER TABLE `picklist` DISABLE KEYS */;
/*!40000 ALTER TABLE `picklist` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `picklist_item`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `picklist_item` (
  `id` char(38) DEFAULT NULL,
  `quantity` int(11) DEFAULT NULL,
  `picklist_id` char(38) DEFAULT NULL,
  `request_item_id` char(38) DEFAULT NULL,
  `inventory_item_id` char(38) DEFAULT NULL,
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `last_updated` datetime NOT NULL,
  `created_by_id` char(38) DEFAULT NULL,
  `updated_by_id` char(38) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `reason_code` varchar(255) DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `picklist_item`
--

LOCK TABLES `picklist_item` WRITE;
/*!40000 ALTER TABLE `picklist_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `picklist_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `product` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `category_id` char(38) DEFAULT NULL,
  `date_created` datetime NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `last_updated` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `product_code` varchar(255) DEFAULT NULL,
  `unit_of_measure_id` char(38) DEFAULT NULL,
  `cold_chain` bit(1) DEFAULT NULL,
  `manufacturer` varchar(255) DEFAULT NULL,
  `manufacturer_code` varchar(255) DEFAULT NULL,
  `ndc` varchar(255) DEFAULT NULL,
  `upc` varchar(255) DEFAULT NULL,
  `unit_of_measure` varchar(255) DEFAULT NULL,
  `created_by_id` char(38) DEFAULT NULL,
  `updated_by_id` char(38) DEFAULT NULL,
  `default_uom_id` char(38) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKED8DCCEFEF4C770D` (`category_id`),
  CONSTRAINT `FKED8DCCEFEF4C770D` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;




--
-- Table structure for table `product_attribute`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `product_attribute` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `attribute_id` char(38) DEFAULT NULL,
  `product_id` char(38) DEFAULT NULL,
  `attributes_idx` int(11) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK94A534CDED5FAE7` (`product_id`),
  KEY `FK94A534C47B0D087` (`attribute_id`),
  CONSTRAINT `FK94A534C47B0D087` FOREIGN KEY (`attribute_id`) REFERENCES `attribute` (`id`),
  CONSTRAINT `FK94A534CDED5FAE7` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_attribute`
--

LOCK TABLES `product_attribute` WRITE;
/*!40000 ALTER TABLE `product_attribute` DISABLE KEYS */;
/*!40000 ALTER TABLE `product_attribute` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_category`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `product_category` (
  `product_id` char(38) DEFAULT NULL,
  `category_id` char(38) DEFAULT NULL,
  `categories_idx` int(11) DEFAULT NULL,
  KEY `FKA0303E4EDED5FAE7` (`product_id`),
  KEY `FKA0303E4EEF4C770D` (`category_id`),
  CONSTRAINT `FKA0303E4EEF4C770D` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_category`
--

LOCK TABLES `product_category` WRITE;
/*!40000 ALTER TABLE `product_category` DISABLE KEYS */;
/*!40000 ALTER TABLE `product_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_document`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `product_document` (
  `product_id` char(38) DEFAULT NULL,
  `document_id` char(38) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_document`
--

LOCK TABLES `product_document` WRITE;
/*!40000 ALTER TABLE `product_document` DISABLE KEYS */;
/*!40000 ALTER TABLE `product_document` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_group`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `product_group` (
  `id` char(38) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `last_updated` datetime NOT NULL,
  `category_id` char(38) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_group`
--

LOCK TABLES `product_group` WRITE;
/*!40000 ALTER TABLE `product_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `product_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_group_product`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `product_group_product` (
  `product_group_id` char(38) DEFAULT NULL,
  `product_id` char(38) DEFAULT NULL,
  `products_idx` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_group_product`
--

LOCK TABLES `product_group_product` WRITE;
/*!40000 ALTER TABLE `product_group_product` DISABLE KEYS */;
/*!40000 ALTER TABLE `product_group_product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_package`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `product_package` (
  `id` char(38) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `gtin` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `uom_id` char(38) DEFAULT NULL,
  `product_id` char(38) DEFAULT NULL,
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `last_updated` datetime NOT NULL,
  `created_by_id` char(38) DEFAULT NULL,
  `updated_by_id` char(38) DEFAULT NULL,
  `quantity` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_package`
--

LOCK TABLES `product_package` WRITE;
/*!40000 ALTER TABLE `product_package` DISABLE KEYS */;
/*!40000 ALTER TABLE `product_package` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_tags`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `product_tags` (
  `product_id` char(38) DEFAULT NULL,
  `tags_string` varchar(255) DEFAULT NULL,
  KEY `FK3C78DD69DED5FAE7` (`product_id`),
  CONSTRAINT `FK3C78DD69DED5FAE7` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_tags`
--

LOCK TABLES `product_tags` WRITE;
/*!40000 ALTER TABLE `product_tags` DISABLE KEYS */;
/*!40000 ALTER TABLE `product_tags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `receipt`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `receipt` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `actual_delivery_date` datetime DEFAULT NULL,
  `date_created` datetime NOT NULL,
  `expected_delivery_date` datetime DEFAULT NULL,
  `last_updated` datetime NOT NULL,
  `recipient_id` char(38) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4082723844979D51` (`recipient_id`),
  CONSTRAINT `FK4082723844979D51` FOREIGN KEY (`recipient_id`) REFERENCES `person` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receipt`
--

LOCK TABLES `receipt` WRITE;
/*!40000 ALTER TABLE `receipt` DISABLE KEYS */;
/*!40000 ALTER TABLE `receipt` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `receipt_item`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `receipt_item` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `comment_id` char(38) DEFAULT NULL,
  `date_created` datetime NOT NULL,
  `expiration_date` datetime DEFAULT NULL,
  `last_updated` datetime NOT NULL,
  `lot_number` varchar(255) DEFAULT NULL,
  `product_id` char(38) DEFAULT NULL,
  `quantity_shipped` int(11) DEFAULT NULL,
  `quantity_received` int(11) NOT NULL,
  `receipt_id` char(38) DEFAULT NULL,
  `recipient_id` char(38) DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `shipment_item_id` char(38) DEFAULT NULL,
  `inventory_item_id` char(38) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKAE3064BA44979D51` (`recipient_id`),
  KEY `FKAE3064BADED5FAE7` (`product_id`),
  KEY `FKAE3064BAF7076438` (`receipt_id`),
  CONSTRAINT `FKAE3064BA44979D51` FOREIGN KEY (`recipient_id`) REFERENCES `person` (`id`),
  CONSTRAINT `FKAE3064BADED5FAE7` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `FKAE3064BAF7076438` FOREIGN KEY (`receipt_id`) REFERENCES `receipt` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receipt_item`
--

LOCK TABLES `receipt_item` WRITE;
/*!40000 ALTER TABLE `receipt_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `receipt_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reference_number`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `reference_number` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `identifier` varchar(255) DEFAULT NULL,
  `reference_number_type_id` char(38) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKD790DEBD154F600` (`reference_number_type_id`),
  CONSTRAINT `FKD790DEBD154F600` FOREIGN KEY (`reference_number_type_id`) REFERENCES `reference_number_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reference_number`
--

LOCK TABLES `reference_number` WRITE;
/*!40000 ALTER TABLE `reference_number` DISABLE KEYS */;
/*!40000 ALTER TABLE `reference_number` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reference_number_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `reference_number_type` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `last_updated` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `sort_order` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reference_number_type`
--

LOCK TABLES `reference_number_type` WRITE;
/*!40000 ALTER TABLE `reference_number_type` DISABLE KEYS */;
INSERT INTO `reference_number_type` VALUES ('1',0,'2010-08-25 00:00:00','Purchase Order Number','2010-08-25 00:00:00','Purchase Order Number|fr:Nombre de commande',0),('2',0,'2010-08-25 00:00:00','Customer name','2010-08-25 00:00:00','Customer name|fr:Nom du client',0),('3',0,'2010-08-25 00:00:00','Internal Identifier','2010-08-25 00:00:00','Internal Identifier|fr:Identificateur interne',0),('4',0,'2010-08-25 00:00:00','Bill of Lading Number','2010-08-25 00:00:00','Bill of Lading Number|fr:Nombre de connaissement',0),('5',0,'2011-03-01 00:00:00','Air Waybill Number','2011-03-01 00:00:00','Air Waybill Number|fr:Nombre de lettre de transport aérien',0),('6',0,'2011-03-01 00:00:00','Container Number','2011-03-01 00:00:00','Container Number|fr:Nombre de conteneur',0),('7',0,'2011-03-01 00:00:00','Seal Number','2011-03-01 00:00:00','Seal Number|fr:Nombre de sceau',0),('8',0,'2011-03-01 00:00:00','Flight Number','2011-03-01 00:00:00','Flight Number|fr:Nombre de vol',0),('9',0,'2012-01-26 00:00:00','License Plate Number|fr:Plaque','2012-01-26 00:00:00','License Plate Number',0);
/*!40000 ALTER TABLE `reference_number_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `request`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `request` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `date_created` datetime DEFAULT NULL,
  `date_requested` datetime DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `destination_id` char(38) DEFAULT NULL,
  `last_updated` datetime DEFAULT NULL,
  `origin_id` char(38) DEFAULT NULL,
  `recipient_id` char(38) DEFAULT NULL,
  `request_number` varchar(255) DEFAULT NULL,
  `requested_by_id` char(38) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `fulfillment_id` char(38) DEFAULT NULL,
  `created_by_id` char(38) DEFAULT NULL,
  `updated_by_id` char(38) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `date_valid_from` datetime DEFAULT NULL,
  `date_valid_to` datetime DEFAULT NULL,
  `recipient_program` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK414EF28F1E2B3CDC` (`destination_id`),
  KEY `FK414EF28F44979D51` (`recipient_id`),
  KEY `FK414EF28F94567276` (`fulfillment_id`),
  KEY `FK414EF28FDBDEDAC4` (`origin_id`),
  KEY `FK414EF28FDD302242` (`requested_by_id`),
  CONSTRAINT `FK414EF28F1E2B3CDC` FOREIGN KEY (`destination_id`) REFERENCES `location` (`id`),
  CONSTRAINT `FK414EF28F44979D51` FOREIGN KEY (`recipient_id`) REFERENCES `person` (`id`),
  CONSTRAINT `FK414EF28F94567276` FOREIGN KEY (`fulfillment_id`) REFERENCES `fulfillment` (`id`),
  CONSTRAINT `FK414EF28FDBDEDAC4` FOREIGN KEY (`origin_id`) REFERENCES `location` (`id`),
  CONSTRAINT `FK414EF28FDD302242` FOREIGN KEY (`requested_by_id`) REFERENCES `person` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `request`
--

LOCK TABLES `request` WRITE;
/*!40000 ALTER TABLE `request` DISABLE KEYS */;
/*!40000 ALTER TABLE `request` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `request_comment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `request_comment` (
  `request_comments_id` char(38) DEFAULT NULL,
  `comment_id` char(38) DEFAULT NULL,
  KEY `FK5BAC88AF87AC2D52` (`request_comments_id`),
  KEY `FK5BAC88AFC4A49BBF` (`comment_id`),
  CONSTRAINT `FK5BAC88AF87AC2D52` FOREIGN KEY (`request_comments_id`) REFERENCES `request` (`id`),
  CONSTRAINT `FK5BAC88AFC4A49BBF` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `request_comment`
--

LOCK TABLES `request_comment` WRITE;
/*!40000 ALTER TABLE `request_comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `request_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `request_document`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `request_document` (
  `request_documents_id` char(38) DEFAULT NULL,
  `document_id` char(38) DEFAULT NULL,
  KEY `FK712980CB2777B76E` (`request_documents_id`),
  KEY `FK712980CBC800AA15` (`document_id`),
  CONSTRAINT `FK712980CBC800AA15` FOREIGN KEY (`document_id`) REFERENCES `document` (`id`),
  CONSTRAINT `FK712980CB2777B76E` FOREIGN KEY (`request_documents_id`) REFERENCES `request` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `request_document`
--

LOCK TABLES `request_document` WRITE;
/*!40000 ALTER TABLE `request_document` DISABLE KEYS */;
/*!40000 ALTER TABLE `request_document` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `request_event`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `request_event` (
  `request_events_id` char(38) DEFAULT NULL,
  `event_id` char(38) DEFAULT NULL,
  KEY `FK674F60EA786431F` (`event_id`),
  KEY `FK674F60EAD962700D` (`request_events_id`),
  CONSTRAINT `FK674F60EA786431F` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`),
  CONSTRAINT `FK674F60EAD962700D` FOREIGN KEY (`request_events_id`) REFERENCES `request` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `request_event`
--

LOCK TABLES `request_event` WRITE;
/*!40000 ALTER TABLE `request_event` DISABLE KEYS */;
/*!40000 ALTER TABLE `request_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `request_item`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `request_item` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `category_id` char(38) DEFAULT NULL,
  `date_created` datetime NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `inventory_item_id` char(38) DEFAULT NULL,
  `last_updated` datetime NOT NULL,
  `product_id` char(38) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
  `request_id` char(38) DEFAULT NULL,
  `requested_by_id` char(38) DEFAULT NULL,
  `unit_price` float(12,0) DEFAULT NULL,
  `product_group_id` char(38) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4DA982C35DE21C87` (`request_id`),
  KEY `FK4DA982C3911E7578` (`requested_by_id`),
  KEY `FK4DA982C3AA992CED` (`inventory_item_id`),
  KEY `FK4DA982C3DED5FAE7` (`product_id`),
  KEY `FK4DA982C3EF4C770D` (`category_id`),
  CONSTRAINT `FK4DA982C35DE21C87` FOREIGN KEY (`request_id`) REFERENCES `request` (`id`),
  CONSTRAINT `FK4DA982C3911E7578` FOREIGN KEY (`requested_by_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK4DA982C3AA992CED` FOREIGN KEY (`inventory_item_id`) REFERENCES `inventory_item` (`id`),
  CONSTRAINT `FK4DA982C3DED5FAE7` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `FK4DA982C3EF4C770D` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `request_item`
--

LOCK TABLES `request_item` WRITE;
/*!40000 ALTER TABLE `request_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `request_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `request_shipment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `request_shipment` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `request_item_id` char(38) DEFAULT NULL,
  `shipment_item_id` char(38) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1F06214A47EBE106` (`request_item_id`),
  KEY `FK1F06214AB06EC4FB` (`shipment_item_id`),
  CONSTRAINT `FK1F06214AB06EC4FB` FOREIGN KEY (`shipment_item_id`) REFERENCES `shipment_item` (`id`),
  CONSTRAINT `FK1F06214A47EBE106` FOREIGN KEY (`request_item_id`) REFERENCES `request_item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `request_shipment`
--

LOCK TABLES `request_shipment` WRITE;
/*!40000 ALTER TABLE `request_shipment` DISABLE KEYS */;
/*!40000 ALTER TABLE `request_shipment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `role` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `role_type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES ('1',0,NULL,'ROLE_ADMIN'),('2',0,NULL,'ROLE_MANAGER'),('3',0,NULL,'ROLE_USER');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shipment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shipment` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `carrier_id` char(38) DEFAULT NULL,
  `date_created` datetime DEFAULT NULL,
  `destination_id` char(38) DEFAULT NULL,
  `donor_id` char(38) DEFAULT NULL,
  `expected_delivery_date` datetime DEFAULT NULL,
  `expected_shipping_date` datetime DEFAULT NULL,
  `last_updated` datetime DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `origin_id` char(38) DEFAULT NULL,
  `recipient_id` char(38) DEFAULT NULL,
  `shipment_method_id` char(38) DEFAULT NULL,
  `shipment_number` varchar(255) DEFAULT NULL,
  `shipment_type_id` char(38) DEFAULT NULL,
  `total_value` float(12,2) DEFAULT NULL,
  `additional_information` longtext,
  `receipt_id` char(38) DEFAULT NULL,
  `stated_value` float(12,2) DEFAULT NULL,
  `weight` float(12,0) DEFAULT NULL,
  `weight_units` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKE139719A1E2B3CDC` (`destination_id`),
  KEY `FKE139719A294C1012` (`carrier_id`),
  KEY `FKE139719A44979D51` (`recipient_id`),
  KEY `FKE139719A49AB6B52` (`donor_id`),
  KEY `FKE139719ADBDEDAC4` (`origin_id`),
  KEY `FKE139719AF7076438` (`receipt_id`),
  KEY `FKE139719AFF77FF9B` (`shipment_type_id`),
  KEY `FKE139719AA28CC5FB` (`shipment_method_id`),
  CONSTRAINT `FKE139719AF7076438` FOREIGN KEY (`receipt_id`) REFERENCES `receipt` (`id`),
  CONSTRAINT `FKE139719A1E2B3CDC` FOREIGN KEY (`destination_id`) REFERENCES `location` (`id`),
  CONSTRAINT `FKE139719A294C1012` FOREIGN KEY (`carrier_id`) REFERENCES `person` (`id`),
  CONSTRAINT `FKE139719A44979D51` FOREIGN KEY (`recipient_id`) REFERENCES `person` (`id`),
  CONSTRAINT `FKE139719A49AB6B52` FOREIGN KEY (`donor_id`) REFERENCES `donor` (`id`),
  CONSTRAINT `FKE139719AA28CC5FB` FOREIGN KEY (`shipment_method_id`) REFERENCES `shipment_method` (`id`),
  CONSTRAINT `FKE139719ADBDEDAC4` FOREIGN KEY (`origin_id`) REFERENCES `location` (`id`),
  CONSTRAINT `FKE139719AFF77FF9B` FOREIGN KEY (`shipment_type_id`) REFERENCES `shipment_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shipment`
--

LOCK TABLES `shipment` WRITE;
/*!40000 ALTER TABLE `shipment` DISABLE KEYS */;
/*!40000 ALTER TABLE `shipment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shipment_comment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shipment_comment` (
  `shipment_comments_id` char(38) DEFAULT NULL,
  `comment_id` char(38) DEFAULT NULL,
  `comments_idx` int(11) DEFAULT NULL,
  KEY `FKC398CCBAC4A49BBF` (`comment_id`),
  CONSTRAINT `FKC398CCBAC4A49BBF` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shipment_comment`
--

LOCK TABLES `shipment_comment` WRITE;
/*!40000 ALTER TABLE `shipment_comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `shipment_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shipment_document`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shipment_document` (
  `shipment_documents_id` char(38) DEFAULT NULL,
  `document_id` char(38) DEFAULT NULL,
  `documents_idx` int(11) DEFAULT NULL,
  KEY `FK6C5BE20C800AA15` (`document_id`),
  CONSTRAINT `FK6C5BE20C800AA15` FOREIGN KEY (`document_id`) REFERENCES `document` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shipment_document`
--

LOCK TABLES `shipment_document` WRITE;
/*!40000 ALTER TABLE `shipment_document` DISABLE KEYS */;
/*!40000 ALTER TABLE `shipment_document` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shipment_event`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shipment_event` (
  `shipment_events_id` char(38) DEFAULT NULL,
  `event_id` char(38) DEFAULT NULL,
  KEY `FK6D032BB53B350242` (`shipment_events_id`),
  KEY `FK6D032BB5786431F` (`event_id`),
  CONSTRAINT `FK6D032BB53B350242` FOREIGN KEY (`shipment_events_id`) REFERENCES `shipment` (`id`),
  CONSTRAINT `FK6D032BB5786431F` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shipment_event`
--

LOCK TABLES `shipment_event` WRITE;
/*!40000 ALTER TABLE `shipment_event` DISABLE KEYS */;
/*!40000 ALTER TABLE `shipment_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shipment_item`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shipment_item` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `container_id` char(38) DEFAULT NULL,
  `date_created` datetime NOT NULL,
  `donor_id` char(38) DEFAULT NULL,
  `last_updated` datetime NOT NULL,
  `lot_number` varchar(255) DEFAULT NULL,
  `product_id` char(38) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
  `recipient_id` char(38) DEFAULT NULL,
  `shipment_items_idx` int(11) DEFAULT NULL,
  `shipment_id` char(38) DEFAULT NULL,
  `expiration_date` datetime DEFAULT NULL,
  `inventory_item_id` char(38) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKDA3BB2983B5F6286` (`shipment_id`),
  KEY `FKDA3BB29844979D51` (`recipient_id`),
  KEY `FKDA3BB29849AB6B52` (`donor_id`),
  KEY `FKDA3BB2987400E88E` (`container_id`),
  KEY `FKDA3BB298DED5FAE7` (`product_id`),
  CONSTRAINT `FKDA3BB2983B5F6286` FOREIGN KEY (`shipment_id`) REFERENCES `shipment` (`id`),
  CONSTRAINT `FKDA3BB29844979D51` FOREIGN KEY (`recipient_id`) REFERENCES `person` (`id`),
  CONSTRAINT `FKDA3BB29849AB6B52` FOREIGN KEY (`donor_id`) REFERENCES `donor` (`id`),
  CONSTRAINT `FKDA3BB2987400E88E` FOREIGN KEY (`container_id`) REFERENCES `container` (`id`),
  CONSTRAINT `FKDA3BB298DED5FAE7` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shipment_item`
--

LOCK TABLES `shipment_item` WRITE;
/*!40000 ALTER TABLE `shipment_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `shipment_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shipment_method`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shipment_method` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `last_updated` datetime NOT NULL,
  `shipper_service_id` char(38) DEFAULT NULL,
  `tracking_number` varchar(255) DEFAULT NULL,
  `shipper_id` char(38) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK40203B263896C98E` (`shipper_id`),
  KEY `FK40203B26296B2CA3` (`shipper_service_id`),
  CONSTRAINT `FK40203B263896C98E` FOREIGN KEY (`shipper_id`) REFERENCES `shipper` (`id`),
  CONSTRAINT `FK40203B26296B2CA3` FOREIGN KEY (`shipper_service_id`) REFERENCES `shipper_service` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shipment_method`
--

LOCK TABLES `shipment_method` WRITE;
/*!40000 ALTER TABLE `shipment_method` DISABLE KEYS */;
/*!40000 ALTER TABLE `shipment_method` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shipment_reference_number`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shipment_reference_number` (
  `shipment_reference_numbers_id` char(38) DEFAULT NULL,
  `reference_number_id` char(38) DEFAULT NULL,
  `reference_numbers_idx` int(11) DEFAULT NULL,
  KEY `FK312F6C292388BC5` (`reference_number_id`),
  CONSTRAINT `FK312F6C292388BC5` FOREIGN KEY (`reference_number_id`) REFERENCES `reference_number` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shipment_reference_number`
--

LOCK TABLES `shipment_reference_number` WRITE;
/*!40000 ALTER TABLE `shipment_reference_number` DISABLE KEYS */;
/*!40000 ALTER TABLE `shipment_reference_number` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shipment_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shipment_type` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `last_updated` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `sort_order` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shipment_type`
--

LOCK TABLES `shipment_type` WRITE;
/*!40000 ALTER TABLE `shipment_type` DISABLE KEYS */;
INSERT INTO `shipment_type` VALUES ('1',1,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Air|fr:Air',1),('2',1,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Sea|fr:Mer',2),('3',0,'2011-08-22 00:00:00','','2011-08-22 00:00:00','Land|fr:Terrains',3),('4',1,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Suitcase|fr:Valise/Malette',4);
/*!40000 ALTER TABLE `shipment_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shipment_workflow`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shipment_workflow` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `last_updated` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `shipment_type_id` char(38) DEFAULT NULL,
  `excluded_fields` varchar(255) DEFAULT NULL,
  `document_template` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `shipment_type_id` (`shipment_type_id`),
  KEY `FKD584C4C4FF77FF9B` (`shipment_type_id`),
  CONSTRAINT `FKD584C4C4FF77FF9B` FOREIGN KEY (`shipment_type_id`) REFERENCES `shipment_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shipment_workflow`
--

LOCK TABLES `shipment_workflow` WRITE;
/*!40000 ALTER TABLE `shipment_workflow` DISABLE KEYS */;
INSERT INTO `shipment_workflow` VALUES ('1',0,'2011-02-28 00:00:00','2011-02-28 00:00:00','Air shipment workflow','1','carrier,recipient',NULL),('2',0,'2011-02-28 00:00:00','2011-02-28 00:00:00','Sea shipment workflow','2','carrier,recipient',NULL),('3',0,'2011-02-28 00:00:00','2011-02-28 00:00:00','Suitcase shipment workflow','4','shipmentMethod.shipper','suitcaseLetter'),('4',0,'2012-01-26 00:00:00','2012-01-26 00:00:00','Land shipment workflow','3',NULL,NULL);
/*!40000 ALTER TABLE `shipment_workflow` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shipment_workflow_container_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shipment_workflow_container_type` (
  `shipment_workflow_container_types_id` char(38) DEFAULT NULL,
  `container_type_id` char(38) DEFAULT NULL,
  `container_types_idx` int(11) DEFAULT NULL,
  KEY `FKDEF5AD1317A6E251` (`container_type_id`),
  CONSTRAINT `FKDEF5AD1317A6E251` FOREIGN KEY (`container_type_id`) REFERENCES `container_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shipment_workflow_container_type`
--

LOCK TABLES `shipment_workflow_container_type` WRITE;
/*!40000 ALTER TABLE `shipment_workflow_container_type` DISABLE KEYS */;
INSERT INTO `shipment_workflow_container_type` VALUES ('1','2',0),('1','8',1),('2','2',0),('2','8',1),('3','3',0);
/*!40000 ALTER TABLE `shipment_workflow_container_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shipment_workflow_reference_number_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shipment_workflow_reference_number_type` (
  `shipment_workflow_reference_number_types_id` char(38) DEFAULT NULL,
  `reference_number_type_id` char(38) DEFAULT NULL,
  `reference_number_types_idx` int(11) DEFAULT NULL,
  KEY `FK4BB27241154F600` (`reference_number_type_id`),
  CONSTRAINT `FK4BB27241154F600` FOREIGN KEY (`reference_number_type_id`) REFERENCES `reference_number_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shipment_workflow_reference_number_type`
--

LOCK TABLES `shipment_workflow_reference_number_type` WRITE;
/*!40000 ALTER TABLE `shipment_workflow_reference_number_type` DISABLE KEYS */;
INSERT INTO `shipment_workflow_reference_number_type` VALUES ('1','5',0),('1','6',1),('1','7',2),('2','4',0),('2','6',1),('2','7',2),('3','8',0),('4','9',0);
/*!40000 ALTER TABLE `shipment_workflow_reference_number_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shipper`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shipper` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `date_created` datetime DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `last_updated` datetime DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `parameter_name` varchar(255) DEFAULT NULL,
  `tracking_format` varchar(255) DEFAULT NULL,
  `tracking_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shipper`
--

LOCK TABLES `shipper` WRITE;
/*!40000 ALTER TABLE `shipper` DISABLE KEYS */;
INSERT INTO `shipper` VALUES ('1',1,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','FedEx',NULL,'999999999999','http://www.fedex.com/Tracking?ascend_header=1&clienttype=dotcom&cntry_code=us&language=english&tracknumbers=%s'),('2',1,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','UPS',NULL,'1Z9999W99999999999','http://wwwapps.ups.com/WebTracking/processInputRequest?sort_by=status&tracknums_displayed=1&TypeOfInquiryNumber=T&loc=en_US&InquiryNumber1=%s&track.x=0&track.y=0'),('3',1,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','DHL','q',NULL,'http://www.google.com/search?hl=en&site=&q='),('4',1,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','USPS','q',NULL,'http://www.google.com/search?hl=en&site=&q='),('5',1,'2010-08-25 00:00:00',NULL,'2010-08-25 00:00:00','Courier','q',NULL,'http://www.google.com/search?hl=en&site=&q=');
/*!40000 ALTER TABLE `shipper` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shipper_service`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shipper_service` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `shipper_id` char(38) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKDF7559D73896C98E` (`shipper_id`),
  CONSTRAINT `FKDF7559D73896C98E` FOREIGN KEY (`shipper_id`) REFERENCES `shipper` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shipper_service`
--

LOCK TABLES `shipper_service` WRITE;
/*!40000 ALTER TABLE `shipper_service` DISABLE KEYS */;
INSERT INTO `shipper_service` VALUES ('1',1,'Same Day Delivery','Same Day Air','1'),('10',1,'3-5 Business Days','Ground','4'),('11',1,NULL,'International Flight','5'),('2',1,'Next Day Delivery','Express Freight','1'),('3',1,'3-5 Business Days','Ground','1'),('4',1,'Same Day Delivery','Same Day Air','2'),('5',1,'Next Day Delivery','Express Freight','2'),('6',1,'3-5 Business Days','Ground','2'),('7',1,'Same Day Delivery','Same Day Air','3'),('8',1,'Next Day Delivery','Express Freight','3'),('9',1,'3-5 Business Days','Ground','3');
/*!40000 ALTER TABLE `shipper_service` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transaction`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transaction` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `created_by_id` char(38) DEFAULT NULL,
  `date_created` datetime NOT NULL,
  `destination_id` char(38) DEFAULT NULL,
  `inventory_id` char(38) DEFAULT NULL,
  `last_updated` datetime NOT NULL,
  `source_id` char(38) DEFAULT NULL,
  `transaction_date` datetime NOT NULL,
  `transaction_type_id` char(38) DEFAULT NULL,
  `confirmed` bit(1) DEFAULT NULL,
  `confirmed_by_id` char(38) DEFAULT NULL,
  `date_confirmed` datetime DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `incoming_shipment_id` char(38) DEFAULT NULL,
  `outgoing_shipment_id` char(38) DEFAULT NULL,
  `updated_by_id` char(38) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7FA0D2DE10746729` (`destination_id`),
  KEY `FK7FA0D2DE74CDABFC` (`source_id`),
  KEY `FK7FA0D2DE217F5972` (`created_by_id`),
  KEY `FK7FA0D2DE3265A8A9` (`confirmed_by_id`),
  KEY `FK7FA0D2DE72A2C5B4` (`inventory_id`),
  KEY `FK7FA0D2DEB3FB7111` (`transaction_type_id`),
  CONSTRAINT `FK7FA0D2DE1E2B3CDC` FOREIGN KEY (`destination_id`) REFERENCES `location` (`id`),
  CONSTRAINT `FK7FA0D2DE217F5972` FOREIGN KEY (`created_by_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK7FA0D2DE3265A8A9` FOREIGN KEY (`confirmed_by_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK7FA0D2DE72A2C5B4` FOREIGN KEY (`inventory_id`) REFERENCES `inventory` (`id`),
  CONSTRAINT `FK7FA0D2DE828481AF` FOREIGN KEY (`source_id`) REFERENCES `location` (`id`),
  CONSTRAINT `FK7FA0D2DEB3FB7111` FOREIGN KEY (`transaction_type_id`) REFERENCES `transaction_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transaction`
--

LOCK TABLES `transaction` WRITE;
/*!40000 ALTER TABLE `transaction` DISABLE KEYS */;
/*!40000 ALTER TABLE `transaction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transaction_entry`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transaction_entry` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `inventory_item_id` char(38) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
  `transaction_id` char(38) DEFAULT NULL,
  `comments` varchar(255) DEFAULT NULL,
  `transaction_entries_idx` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKABC21FD12EF4C7F4` (`transaction_id`),
  KEY `FKABC21FD1AA992CED` (`inventory_item_id`),
  CONSTRAINT `FKABC21FD1AA992CED` FOREIGN KEY (`inventory_item_id`) REFERENCES `inventory_item` (`id`),
  CONSTRAINT `FKABC21FD12EF4C7F4` FOREIGN KEY (`transaction_id`) REFERENCES `transaction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transaction_entry`
--

LOCK TABLES `transaction_entry` WRITE;
/*!40000 ALTER TABLE `transaction_entry` DISABLE KEYS */;
/*!40000 ALTER TABLE `transaction_entry` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transaction_type`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transaction_type` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `last_updated` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `sort_order` int(11) DEFAULT NULL,
  `transaction_code` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transaction_type`
--

LOCK TABLES `transaction_type` WRITE;
/*!40000 ALTER TABLE `transaction_type` DISABLE KEYS */;
INSERT INTO `transaction_type` VALUES ('10',0,'2011-04-05 00:00:00',NULL,'2011-04-05 00:00:00','Adjustment - Debit|fr:Ajustement - Débit',0,'DEBIT'),('11',0,'2011-04-06 00:00:00',NULL,'2011-04-06 00:00:00','Product Inventory|fr:Inventaire de Produits',0,'PRODUCT_INVENTORY'),('2',0,'2010-11-08 00:00:00',NULL,'2010-11-08 00:00:00','Consumption|fr:Consommation',0,'DEBIT'),('3',0,'2010-11-08 00:00:00',NULL,'2010-11-08 00:00:00','Adjustment - Credit|fr:Ajustement - Crédit',0,'CREDIT'),('4',0,'2010-11-08 00:00:00',NULL,'2010-11-08 00:00:00','Expired|fr:Espiré',0,'DEBIT'),('5',0,'2010-11-08 00:00:00',NULL,'2010-11-08 00:00:00','Damaged|fr:Endommagé',0,'DEBIT'),('7',0,'2010-12-06 00:00:00','Inventory','2010-12-06 00:00:00','Inventory|fr:Inventaire',0,'INVENTORY'),('8',0,'2011-03-17 00:00:00',NULL,'2011-03-17 00:00:00','Transfer In|fr:Transférer en',0,'CREDIT'),('9',0,'2011-03-17 00:00:00',NULL,'2011-03-17 00:00:00','Transfer Out|fr:Transférer hors',0,'DEBIT');
/*!40000 ALTER TABLE `transaction_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `unit_of_measure`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `unit_of_measure` (
  `id` char(38) NOT NULL DEFAULT '',
  `version` bigint(20) NOT NULL,
  `code` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `uom_class_id` char(38) DEFAULT NULL,
  `date_created` datetime NOT NULL,
  `last_updated` datetime NOT NULL,
  `created_by_id` char(38) DEFAULT NULL,
  `updated_by_id` char(38) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `unit_of_measure`
--

LOCK TABLES `unit_of_measure` WRITE;
/*!40000 ALTER TABLE `unit_of_measure` DISABLE KEYS */;
/*!40000 ALTER TABLE `unit_of_measure` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `unit_of_measure_class`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `unit_of_measure_class` (
  `id` char(38) DEFAULT NULL,
  `active` bit(1) DEFAULT b'1',
  `name` varchar(255) DEFAULT NULL,
  `code` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `base_uom_id` char(38) DEFAULT NULL,
  `version` bigint(20) NOT NULL,
  `date_created` datetime NOT NULL,
  `last_updated` datetime NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  `created_by_id` char(38) DEFAULT NULL,
  `updated_by_id` char(38) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `unit_of_measure_class`
--

LOCK TABLES `unit_of_measure_class` WRITE;
/*!40000 ALTER TABLE `unit_of_measure_class` DISABLE KEYS */;
/*!40000 ALTER TABLE `unit_of_measure_class` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` char(38) NOT NULL DEFAULT '',
  `active` bit(1) DEFAULT NULL,
  `last_login_date` datetime DEFAULT NULL,
  `manager_id` char(38) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `warehouse_id` char(38) DEFAULT NULL,
  `photo` mediumblob,
  `locale` varchar(255) DEFAULT NULL,
  `remember_last_location` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `FK36EBCB1F28CE07` (`warehouse_id`),
  KEY `FK36EBCB41E07A73` (`manager_id`),
  CONSTRAINT `FK36EBCB1F28CE07` FOREIGN KEY (`warehouse_id`) REFERENCES `location` (`id`),
  CONSTRAINT `FK36EBCB41E07A73` FOREIGN KEY (`manager_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES ('1','',NULL,NULL,'password','admin','1',NULL,NULL,NULL),('2','',NULL,'1','password','manager','4',NULL,NULL,NULL),('3','',NULL,'2','password','jmiranda','1',NULL,NULL,NULL),('4','\0',NULL,'2','password','inactive','1',NULL,NULL,NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_role`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_role` (
  `user_id` char(38) DEFAULT NULL,
  `role_id` char(38) DEFAULT NULL,
  KEY `FK143BF46AA462C195` (`user_id`),
  KEY `FK143BF46AFF37FDB5` (`role_id`),
  CONSTRAINT `FK143BF46AFF37FDB5` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`),
  CONSTRAINT `FK143BF46AA462C195` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
INSERT INTO `user_role` VALUES ('1','1'),('2','2'),('3','3'),('4','3');
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2012-10-11 14:56:42
