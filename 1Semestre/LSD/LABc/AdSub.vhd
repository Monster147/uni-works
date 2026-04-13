library ieee;

use ieee.std_logic_1164.all;

entity AdSub is
port(
	a: in std_logic_vector(3 downto 0);
	b: in std_logic_vector(3 downto 0);
	Opau: in std_logic;
	Cbi: in std_logic;
	cbo: out std_logic;
	s: out std_logic_vector(3 downto 0);
	CHb3: out std_logic
	);
end AdSub;

architecture AdSubimp of AdSub is
component Adder is
port(
 A: in std_logic_vector(3 downto 0);
 B: in std_logic_vector(3 downto 0);
 C0: in std_logic;
 S: out std_logic_vector(3 downto 0);
 C4: out std_logic
 );
end component;
signal CHb:std_logic_vector(3 downto 0);
signal CHcbo:std_logic;
signal CHc0:std_logic;

begin
CHb(0) <= B(0) xor Opau;
CHb(1) <= B(1) xor Opau;
CHb(2) <= B(2) xor Opau;
CHb(3) <= B(3) xor Opau;
CHc0 <= Cbi xor Opau;
cbo <= CHcbo xor Opau ;
CHb3 <= B(3) xor Opau;
U1: Adder port map (a(0) => A(0), a(1) => A(1), a(2) => A(2), a(3) => A(3), b(0) => CHb(0), b(1) => CHb(1), b(2) => CHb(2), b(3) => CHb(3), s(0) => S(0), s(1) => S(1), s(2) => S(2), s(3) => S(3), C0 => CHc0 , C4 => CHcbo);
end AdSubimp;