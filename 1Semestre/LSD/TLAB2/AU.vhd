library ieee;

use ieee.std_logic_1164.all;

entity AU is
 port(
 CBi: in std_logic;
 A: in std_logic_vector(3 downto 0);
 B: in std_logic_vector(3 downto 0);
 OPau: in std_logic;
 R: out std_logic_vector(3 downto 0);
 CBo: out std_logic;
 OV: out std_logic
 );
end AU;

architecture AUcirc of AU is
component AdSub is
port(
	a: in std_logic_vector(3 downto 0);
	b: in std_logic_vector(3 downto 0);
	Opau: in std_logic;
	Cbi: in std_logic;
	cbo: out std_logic;
	s: out std_logic_vector(3 downto 0);
	CHb3: out std_logic
	);
end component;

component Flags is
port(
	a3: in std_logic;
	b3: in std_logic;
	iCBo: in std_logic;
	Ri: in std_logic;
	OVo: out std_logic;
	CBo: out std_logic
 );
end component;

signal s3fio, b3fio, chcbofio:std_logic;

begin
U1: AdSub port map (a => A, b => B, CHb3 => b3fio, Opau => OPau, Cbi => CBi, s(0) => R(0), s(2) => R(2), s(1) => R(1), s(3) => s3fio , cbo => chcbofio);
U2: Flags port map (a3 => A(3), b3 => b3fio, iCBo => chcbofio, Ri => S3fio, OVo => OV, CBo=>CBo);
R(3) <= s3fio;
end AUcirc;