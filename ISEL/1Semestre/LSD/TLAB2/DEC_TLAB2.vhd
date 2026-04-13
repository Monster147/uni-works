library ieee;

use ieee.std_logic_1164.all;

entity DEC_TLAB2 is port(
	OP_DEC: in std_logic_vector(2 downto 0);
	OPa_DEC: out std_logic;
	OPb_DEC: out std_logic;
	OPc_DEC: out std_logic;
	OPd_DEC: out std_logic;
	OPe_DEC: out std_logic;
	OPf_DEC: out std_logic
	);
end DEC_TLAB2;

architecture DEC_TLAB2_ARCH of DEC_TLAB2 is
begin
OPa_DEC <= OP_DEC(1);
OPb_DEC <= OP_DEC(0);
OPc_DEC <= OP_DEC(2);
OPd_DEC <= OP_DEC(0);
OPe_DEC <= OP_DEC(1);
OPf_DEC <= OP_DEC(2);
end DEC_TLAB2_ARCH;