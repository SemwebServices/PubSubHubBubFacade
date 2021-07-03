select 1;

CREATE USER capcollator WITH PASSWORD 'capcollator';
CREATE USER feedfacade WITH PASSWORD 'feedFacade';

DROP DATABASE capcollatorprod;
CREATE DATABASE capcollatorprod;
GRANT ALL PRIVILEGES ON DATABASE capcollatorprod to capcollator;

DROP DATABASE feedfacade;
CREATE DATABASE feedfacade;
GRANT ALL PRIVILEGES ON DATABASE feedfacade to feedfacade;
