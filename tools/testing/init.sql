select 1;

CREATE USER capcollator WITH PASSWORD 'capcollator';
CREATE USER feedfacade WITH PASSWORD 'feedFacade';

DROP DATABASE if exists capcollatorprod;
CREATE DATABASE capcollatorprod;
GRANT ALL PRIVILEGES ON DATABASE capcollatorprod to capcollator;

DROP DATABASE if exists feedfacade;
CREATE DATABASE feedfacade;
GRANT ALL PRIVILEGES ON DATABASE feedfacade to feedfacade;

DROP DATABASE if exists feedfacadedev;
CREATE DATABASE feedfacadedev;
GRANT ALL PRIVILEGES ON DATABASE feedfacadedev to feedfacade;

DROP DATABASE if exists feedfacadetest;
CREATE DATABASE feedfacadetest;
GRANT ALL PRIVILEGES ON DATABASE feedfacadetest to feedfacade;
