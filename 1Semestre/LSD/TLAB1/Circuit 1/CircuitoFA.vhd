library ieee;

use ieee.std_logic_1164.all;

entity CircuitoFA is port(
	A : in std_logic;
	B : in std_logic;
	C : in std_logic;
	S0 : out std_logic;
	S1 : out std_logic
	);
end CircuitoFA;

architecture Funfa of CircuitoFA is
signal term : std_logic_vector(6 downto 0);
begin
term(0) <= (not A) and (not B) and C;
term(1) <= (not A) and B and (not C);
term(2) <= A and (not B) and (not C);
term(3) <= A and B and C;
term(4) <= B and C;
term(5) <= A and C;
term(6) <= A and B;

S0 <= term(0) or term(1) or term(2) or term(3);
S1 <= term(4) or term(5) or term(6);
end Funfa;