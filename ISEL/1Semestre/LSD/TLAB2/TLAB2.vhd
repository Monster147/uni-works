library ieee;

use ieee.std_logic_1164.all;

entity TLAB2 is
 port(
 CBI: in std_logic;
 X: in std_logic_vector(3 downto 0);
 Y: in std_logic_vector(3 downto 0);
 OP: in std_logic_vector(2 downto 0);
 R_TLAB2: out std_logic_vector(3 downto 0);
 CBo_TLAB2: out std_logic;
 OV_TLAB2: out std_logic;
 Z_TLAB2: out std_logic;
 GE_TLAB2: out std_logic;
 BE_TLAB2: out std_logic
 );
end TLAB2;
architecture TLAB2Circ of TLAB2 is
component AU is
port(
	CBi: in std_logic;
	A: in std_logic_vector(3 downto 0);
	B: in std_logic_vector(3 downto 0);
	OPau: in std_logic;
	R: out std_logic_vector(3 downto 0);
	CBo: out std_logic;
	OV: out std_logic
	);
end component;
component Mux_TLAB2 is
port(
	A_Mux : in std_logic_vector(3 downto 0);
	B_Mux : in std_logic_vector(3 downto 0);
	S_Mux : in std_logic;
	Y_Mux : out std_logic_vector(3 downto 0)
	);
end component;
component Flags_TLAB2 is
port(
	iOV_Flags : in std_logic;
	iCB_Flags : in std_logic;
	OP_Flags : in std_logic;
	R_Flags : in std_logic_vector(3 downto 0);
	CY_Flags : in std_logic;
	BE_Flags : out std_logic;
	oGE_Flags : out std_logic;
	Z_Flags : out std_logic;
	oOV_Flags : out std_logic;
	oCB_Flags : out std_logic
	);
end component;
component LogicMod is port(
	A_LogicMod: in std_logic_vector(3 downto 0);
	B_LogicMod: in std_logic_vector(3 downto 0);
	S0_LogicMod: in std_logic;
	S1_LogicMod: in std_logic;
	R_LogicMod: out std_logic_vector(3 downto 0);
	CY_LogicMod: out std_logic
	);
end component;
component Yor0_TLAB2 is port(
	A_Yor0 : in std_logic_vector(3 downto 0);
	S_Yor0 : in std_logic;
	Y_Yor0 : out std_logic_vector(3 downto 0)
	);
end component;
component DEC_TLAB2 is port(
	OP_DEC: in std_logic_vector(2 downto 0);
	OPa_DEC: out std_logic;
	OPb_DEC: out std_logic;
	OPc_DEC: out std_logic;
	OPd_DEC: out std_logic;
	OPe_DEC: out std_logic;
	OPf_DEC: out std_logic
	);
end component;

signal OViOV, CBoiCB, CYcy:std_logic;
signal RA, RB, YR, YB:std_logic_vector(3 downto 0);
signal OPaOPa, OPbOPb, OPcOPc, OPdOPd, OPeOPe, OPfOPf:std_logic;

begin

U1: AU port map (Cbi => CBI, A => X, OV => OViOV, CBo => CBoiCB, R => RA, B => YB, OPau => OPbOPb);
U2: LogicMod port map(A_LogicMod => X, B_LogicMod => Y, R_LogicMod => RB, S0_LogicMod => OPdOPd, S1_LogicMod => OPeOPe, CY_LogicMod => CYcy);
U3: Mux_TLAB2 port map (A_Mux => RA, B_Mux => RB, S_Mux => OPcOPc, Y_Mux => YR);
U4: Flags_TLAB2 port map (R_Flags => YR, CY_Flags => CYcy, iOV_Flags => OViOV, iCB_Flags => CBoiCB, BE_Flags => BE_TLAB2, oGE_Flags => GE_TLAB2, Z_Flags => Z_TLAB2, oOV_Flags => OV_TLAB2, oCB_Flags => CBo_TLAB2, OP_Flags => OPfOPf);
U5: Yor0_TLAB2 port map (A_Yor0 => Y, S_Yor0 => OPaOPa, Y_Yor0 => YB);
U6: DEC_TLAB2 port map (OP_DEC => OP, OPa_DEC => OPaOPa, OPb_DEC => OPbOPb, OPc_DEC => OPcOPc, OPd_DEC => OPdOPd, OPe_DEC => OPeOPe, OPf_DEC => OPfOPf);
R_TLAB2 <= YR;
end TLAB2Circ;