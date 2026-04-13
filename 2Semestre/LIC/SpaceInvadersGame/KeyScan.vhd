 library ieee;
 use ieee.std_logic_1164.all;

entity KeyScan is port (

	KScan: in std_logic;
	Clk: in std_logic;
	RESET: in std_logic;
	KEYPAD_LIN: in std_logic_vector(3 downto 0);
	KEYPAD_COL: out std_logic_vector(2 downto 0);	
	K: out std_logic_vector(3 downto 0);
	KPress: out std_logic
 );
 
end KeyScan;
architecture structural of KeyScan is

component MUX_4_1 is port(
	A : in std_logic ;
	B : in std_logic ;
	C : in std_logic ;
	D : in std_logic ;
	S : in std_logic_vector(1 downto 0);
	Y : out std_logic
);
end component;

component CounterUp is
	port(
	dataIn : in std_logic_vector(3 downto 0);
	PL : in std_logic;
	CE: in std_logic;
	CLK : in std_logic;
	RESET : in std_logic;
	Q : out std_logic_vector(3 downto 0)
	);
end component;

component DEC_2_4 is
port(
	S: in std_logic_vector(1 downto 0);
	E: in std_logic;
	o: out std_logic_vector(3 downto 0)
	);
end component;


signal Saida_Q : std_logic_vector(3 downto 0);
signal Saida_Y, lixo : std_logic;
signal KCOL: std_logic_vector(2 downto 0);


begin
U1 : CounterUp port map (CE => KScan, CLK=>Clk, Q(3 downto 0)=> Saida_Q, RESET => RESET, dataIn => "0000", PL => '0');
U2 : DEC_2_4 port map (S(1) => Saida_Q(3), S(0) => Saida_Q(2), E => '1', o(2 downto 0) => KCOL, o(3) => lixo); 
U3: MUX_4_1 port map (A => KEYPAD_LIN(0), B => KEYPAD_LIN(1), C => KEYPAD_LIN(2), D => KEYPAD_LIN(3), S(1) => Saida_Q(1), S(0) => Saida_Q(0), Y =>Saida_Y);
K(3) <= Saida_Q(3);
K(2) <= not Saida_Q(2);
K(1) <= Saida_Q(1);
K(0) <= not Saida_Q(0);
KPress <= not Saida_Y;
KEYPAD_COL <= not KCOL;
end structural;