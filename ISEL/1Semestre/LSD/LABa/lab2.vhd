library ieee;
use ieee.std_logic_1164.all;
entity lab2 is 
	port(A,B,C : in std_logic;
	F : out std_logic);
end lab2;

architecture lab2_ABCF of lab2 is
signal term : std_logic_vector(1 downto 0);
begin
term(0) <= A or (not B);
term(1) <= (not A) or C;
F <= not (not ( (not term(0)) or (not term(1))));
end lab2_ABCF;